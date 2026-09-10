package com.clinic.assistant;

import com.clinic.assistant.chatbot.ChatbotOrchestrator;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.util.Scanner;

/**
 * CLI entry point — Quarkus Command Mode.
 *
 * This is the OUTER LOOP:
 *   1. Start conversation (print greeting)
 *   2. Read user input
 *   3. Send to ChatbotOrchestrator → get response
 *   4. Print response
 *   5. Repeat until user says "bye", "quit", or "exit"
 *
 * @QuarkusMain tells Quarkus to run this as a CLI app, not a web server.
 */
@QuarkusMain
public class DoctorAssistantApp implements QuarkusApplication {

    @Inject
    ChatbotOrchestrator orchestrator;

    @Override
    public int run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===========================================");
        System.out.println("   Doctor's Assistant - Super Clinic");
        System.out.println("   Type 'quit' or 'exit' to end");
        System.out.println("===========================================");
        System.out.println();

        // Start the conversation — LLM generates the greeting
        String greeting = orchestrator.startConversation();
        System.out.println("Assistant: " + greeting);
        System.out.println();

        // OUTER LOOP: keep chatting until user exits
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.isEmpty()) {
                continue;
            }

            // Exit commands
            if (userInput.equalsIgnoreCase("quit")
                    || userInput.equalsIgnoreCase("exit")
                    || userInput.equalsIgnoreCase("bye")) {
                System.out.println();
                System.out.println("Assistant: Thank you for contacting Super Clinic. Have a good day!");
                break;
            }

            // Send to orchestrator and display response
            try {
                String response = orchestrator.processUserMessage(userInput);
                System.out.println();
                System.out.println("Assistant: " + response);
                System.out.println();
            } catch (Exception e) {
                System.out.println();
                System.out.println("Assistant: I'm sorry, I encountered an error. Please try again.");
                System.err.println("Error: " + e.getMessage());
                System.out.println();
            }
        }

        scanner.close();
        return 0;
    }
}
