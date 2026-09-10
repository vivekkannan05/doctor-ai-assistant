package com.clinic.assistant.chatbot;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The conversation loop — orchestrates the interaction between the user and OpenAI.
 *
 * This is the "beating heart" of the chatbot. It implements the pattern:
 *
 *   User message → Send to OpenAI → Check response:
 *     - If TEXT → return to display
 *     - If TOOL CALL → execute via ToolDispatcher → send result back → loop
 */
@ApplicationScoped
public class ChatbotOrchestrator {

    private static final Logger LOG = Logger.getLogger(ChatbotOrchestrator.class);
    private static final int MAX_TOOL_ROUNDS = 10;

    @Inject
    ToolDispatcher toolDispatcher;

    @ConfigProperty(name = "openai.api.key")
    String apiKey;

    private OpenAIClient client;
    private List<ResponseInputItem> conversationInputs;
    private String lastResponseId;

    private long turnInputTokens;
    private long turnOutputTokens;
    private long sessionInputTokens;
    private long sessionOutputTokens;
    private int turnApiCalls;

    /**
     * System prompt — the "employee handbook" for the receptionist.
     * {TODAY} is replaced with the actual date at runtime.
     */
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a friendly receptionist at "Super Clinic", a multi-specialty medical clinic.
            Your name is "Assistant". You help patients book appointments with doctors.

            Always begin the conversation with: "Hello and welcome to the Super Clinic! How can I help you today?"

            APPOINTMENT BOOKING RULES:
            - Always confirm the doctor, date, and time with the patient BEFORE booking.
            - Never book an appointment without explicit confirmation from the patient (e.g., "yes", "ok", "sure").
            - If a requested slot is unavailable, suggest the nearest available alternative.
            - If a doctor has no availability on the requested date, proactively check nearby dates and offer alternatives.
            - When an appointment is confirmed, collect the patient's name if not already provided.

            SYMPTOM HANDLING:
            - When a patient describes symptoms instead of asking for a specific doctor, identify the most likely medical specialty they need (e.g., rashes -> dermatology, bone/joint issues -> orthopedics, chest pain -> cardiology, headaches -> neurology).
            - Use the GetDoctorsBySpecialty tool to find matching doctors.
            - If the clinic has no doctors for that specialty, politely inform the patient and ask if there's anything else you can help with.
            - You are NOT a doctor. Never diagnose conditions or provide medical advice. Only help route to the right specialist.

            TOOL USAGE:
            - Use GetAllSpecialties to tell patients what specialties are available at the clinic.
            - Use GetDoctorsBySpecialty when a patient needs a type of doctor (by specialty).
            - Use GetDoctorByName when a patient asks for a specific doctor by name.
            - Use GetAvailabilities to check a doctor's open time slots on a specific date.
            - Use BookAppointment ONLY after the patient has explicitly confirmed the doctor, date, and time.
            - NEVER make up or assume doctor availability, schedules, or names. ALWAYS use the provided tools to look up real data.
            - Today's date is {TODAY}. When the patient says "today", "tomorrow", etc., convert to the appropriate YYYY-MM-DD date.

            BOUNDARIES:
            - Only handle appointment-related queries. For billing, insurance, prescriptions, or unrelated topics, politely say "I can only help with appointment booking. Please contact our front desk for other queries."
            - Keep responses concise and conversational.
            - When the conversation is complete, end with: "Thank you for contacting Super Clinic. Have a good day!"
            """;

    @PostConstruct
    void init() {
        client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        conversationInputs = new ArrayList<>();
        LOG.info("ChatbotOrchestrator initialized with OpenAI client");
    }

    /**
     * Starts a new conversation — returns the greeting message.
     */
    public String startConversation() {
        conversationInputs.clear();
        lastResponseId = null;  // Reset thread — new conversation starts fresh
        return processUserMessage("Hello");
    }

    /**
     * Processes a single user message through the full orchestration loop:
     *
     *   1. Add user message to conversation inputs
     *   2. Send to OpenAI with tools + system prompt
     *   3. INNER LOOP: While response has tool calls:
     *      a. Execute each tool call via ToolDispatcher
     *      b. Add tool call + result to inputs
     *      c. Send updated inputs back to OpenAI
     *   4. Extract and return the text response
     */
    public String processUserMessage(String userMessage) {
        // Reset per-turn counters
        turnInputTokens = 0;
        turnOutputTokens = 0;
        turnApiCalls = 0;

        conversationInputs.clear();

        conversationInputs.add(ResponseInputItem.ofMessage(
                ResponseInputItem.Message.builder()
                        .role(ResponseInputItem.Message.Role.USER)
                        .addInputTextContent(userMessage)
                        .build()
        ));

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.replace("{TODAY}", LocalDate.now().toString());

        ResponseCreateParams.Builder paramsBuilder = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .instructions(systemPrompt)
                .addTool(ToolClasses.GetAllDoctors.class)
                .addTool(ToolClasses.GetDoctorByName.class)
                .addTool(ToolClasses.GetAllSpecialties.class)
                .addTool(ToolClasses.GetDoctorsBySpecialty.class)
                .addTool(ToolClasses.GetAvailabilities.class)
                .addTool(ToolClasses.BookAppointment.class)
                .maxOutputTokens(2048)
                .input(ResponseCreateParams.Input.ofResponse(conversationInputs));

        if (lastResponseId != null) {
            paramsBuilder.previousResponseId(lastResponseId);
        }

        Response response = client.responses().create(paramsBuilder.build());
        trackUsage(response, "User message");

        // INNER LOOP — handle tool calls until we get a text response
        int toolRounds = 0;
        while (hasToolCalls(response) && toolRounds < MAX_TOOL_ROUNDS) {
            toolRounds++;
            LOG.debugf("Tool round %d", toolRounds);

            // The response already contains the tool calls.
            // We only need to send back the RESULTS (function call outputs).
            conversationInputs.clear();

            for (ResponseOutputItem item : response.output()) {
                if (item.isFunctionCall()) {
                    ResponseFunctionToolCall functionCall = item.asFunctionCall();
                    LOG.infof("Executing tool: %s", functionCall.name());

                    Object result = toolDispatcher.dispatch(functionCall);

                    conversationInputs.add(ResponseInputItem.ofFunctionCallOutput(
                            ResponseInputItem.FunctionCallOutput.builder()
                                    .callId(functionCall.callId())
                                    .outputAsJson(result)
                                    .build()
                    ));
                }
            }

            // Point to THIS response's ID so the server has full context
            // including the tool calls it just generated
            response = client.responses().create(
                    ResponseCreateParams.builder()
                            .model(ChatModel.GPT_4O)
                            .instructions(systemPrompt)
                            .addTool(ToolClasses.GetAllDoctors.class)
                            .addTool(ToolClasses.GetDoctorByName.class)
                            .addTool(ToolClasses.GetAllSpecialties.class)
                            .addTool(ToolClasses.GetDoctorsBySpecialty.class)
                            .addTool(ToolClasses.GetAvailabilities.class)
                            .addTool(ToolClasses.BookAppointment.class)
                            .maxOutputTokens(2048)
                            .previousResponseId(response.id())
                            .input(ResponseCreateParams.Input.ofResponse(conversationInputs))
                            .build()
            );
            trackUsage(response, "Tool round " + toolRounds);
        }

        if (toolRounds >= MAX_TOOL_ROUNDS) {
            LOG.warn("Reached max tool rounds — forcing text response");
            return "I'm having trouble processing your request. "
                    + "Please contact our front desk for assistance.";
        }

        String assistantText = extractText(response);
        lastResponseId = response.id();
        logTurnSummary();
        return assistantText;
    }

    private void trackUsage(Response response, String label) {
        response.usage().ifPresent(usage -> {
            long in = usage.inputTokens();
            long out = usage.outputTokens();
            turnInputTokens += in;
            turnOutputTokens += out;
            sessionInputTokens += in;
            sessionOutputTokens += out;
            turnApiCalls++;
            LOG.infof("[TOKENS] %s — input: %d, output: %d, total: %d", label, in, out, in + out);
        });
    }

    private void logTurnSummary() {
        LOG.infof("[TOKENS] Turn summary — API calls: %d, input: %d, output: %d, turn total: %d",
                turnApiCalls, turnInputTokens, turnOutputTokens, turnInputTokens + turnOutputTokens);
        LOG.infof("[TOKENS] Session cumulative — input: %d, output: %d, session total: %d",
                sessionInputTokens, sessionOutputTokens, sessionInputTokens + sessionOutputTokens);
    }

    private boolean hasToolCalls(Response response) {
        return response.output().stream().anyMatch(ResponseOutputItem::isFunctionCall);
    }

    private String extractText(Response response) {
        return response.output().stream()
                .filter(ResponseOutputItem::isMessage)
                .flatMap(item -> item.asMessage().content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(textContent -> textContent.text())
                .reduce("", (a, b) -> a + b);
    }
}
