package com.clinic.assistant.chatbot;

import com.clinic.assistant.chatbot.ToolClasses.*;
import com.clinic.assistant.service.ClinicService;
import com.openai.models.responses.ResponseFunctionToolCall;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Routes the LLM's function call requests to the actual ClinicService methods.
 *
 * When the LLM returns a function_call (e.g., name="GetDoctorsBySpecialty", args={specialty:"orthopedics"}),
 * this class:
 *   1. Reads the function name
 *   2. Deserializes the JSON arguments into the matching ToolClass
 *   3. Calls the corresponding ClinicService method
 *   4. Returns the result (which the SDK serializes back to JSON for the LLM)
 */
@ApplicationScoped
public class ToolDispatcher {

    private static final Logger LOG = Logger.getLogger(ToolDispatcher.class);

    @Inject
    ClinicService clinicService;

    /**
     * Dispatches a function call from the LLM to the appropriate service method.
     *
     * @param functionCall The tool call from the LLM response
     * @return The result to send back to the LLM (will be serialized to JSON)
     */
    public Object dispatch(ResponseFunctionToolCall functionCall) {
        String functionName = functionCall.name();
        LOG.infof("Tool call: %s", functionName);

        try {
            switch (functionName) {
                case "GetAllDoctors":
                    return clinicService.getAllDoctors();

                case "GetDoctorByName": {
                    GetDoctorByName args = functionCall.arguments(GetDoctorByName.class);
                    LOG.debugf("  args: doctorName=%s", args.doctorName);
                    return clinicService.getDoctorByName(args.doctorName);
                }

                case "GetAllSpecialties":
                    return clinicService.getAllSpecialties();

                case "GetDoctorsBySpecialty": {
                    GetDoctorsBySpecialty args = functionCall.arguments(GetDoctorsBySpecialty.class);
                    LOG.debugf("  args: specialty=%s", args.specialty);
                    return clinicService.getDoctorsBySpecialty(args.specialty);
                }

                case "GetAvailabilities": {
                    GetAvailabilities args = functionCall.arguments(GetAvailabilities.class);
                    LOG.debugf("  args: doctorName=%s, date=%s", args.doctorName, args.date);
                    return clinicService.getAvailabilities(args.doctorName, args.date);
                }

                case "BookAppointment": {
                    BookAppointment args = functionCall.arguments(BookAppointment.class);
                    LOG.debugf("  args: doctor=%s, date=%s, time=%s, patient=%s",
                            args.doctorName, args.date, args.time, args.patientName);
                    return clinicService.bookAppointment(
                            args.doctorName, args.date, args.time, args.patientName);
                }

                default:
                    LOG.warnf("Unknown function: %s", functionName);
                    return Map.of("error", "Unknown function: " + functionName);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error executing tool: %s", functionName);
            return Map.of("error", "Failed to execute " + functionName + ": " + e.getMessage());
        }
    }
}
