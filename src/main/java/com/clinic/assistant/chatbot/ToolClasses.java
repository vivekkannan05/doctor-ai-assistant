package com.clinic.assistant.chatbot;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Tool definitions for the OpenAI Responses API.
 *
 * Each inner class represents ONE tool the LLM can call.
 * The SDK auto-generates JSON schemas from these classes:
 *   - @JsonClassDescription  → becomes the tool's "description" (LLM reads this to decide WHEN to use it)
 *   - @JsonPropertyDescription → describes each parameter (LLM reads this to fill in values)
 *   - public fields → become the parameters (field type → JSON type)
 *
 * The LLM never sees this Java code. It only sees the generated JSON schema.
 */
public class ToolClasses {

    @JsonClassDescription(
            "Returns a list of all doctors at Super Clinic with their names and specialties. "
            + "Use when the patient asks to see all available doctors or wants a general overview."
    )
    public static class GetAllDoctors {
        @JsonPropertyDescription("Always pass 'all' to retrieve the full list of doctors.")
        public String scope = "all";
    }

    @JsonClassDescription(
            "Finds a specific doctor by name. "
            + "Use when the patient asks about a specific doctor by name."
    )
    public static class GetDoctorByName {
        @JsonPropertyDescription("Name of the doctor to search for, e.g., 'Dr. Sharma' or 'Sharma'")
        public String doctorName;
    }

    @JsonClassDescription(
            "Returns all distinct medical specialties available at Super Clinic. "
            + "Use when the patient asks what types of doctors or specialties are available."
    )
    public static class GetAllSpecialties {
        @JsonPropertyDescription("Always pass 'all' to retrieve the full list of specialties.")
        public String scope = "all";
    }

    @JsonClassDescription(
            "Returns all doctors who specialize in a given medical specialty. "
            + "Use when the patient describes symptoms or asks for a type of doctor. "
            + "For example: rashes → dermatology, bone/joint pain → orthopedics, "
            + "chest pain → cardiology, headaches → neurology."
    )
    public static class GetDoctorsBySpecialty {
        @JsonPropertyDescription(
                "The medical specialty to search for, e.g., 'Orthopedics', 'Cardiology', "
                + "'Dermatology', 'General Medicine', 'Neurology'"
        )
        public String specialty;
    }

    @JsonClassDescription(
            "Returns available (unbooked) time slots for a specific doctor on a specific date. "
            + "Use to check if a doctor can see a patient on a particular day."
    )
    public static class GetAvailabilities {
        @JsonPropertyDescription("Full name of the doctor, e.g., 'Dr. Sharma'")
        public String doctorName;

        @JsonPropertyDescription("Date to check availability for, in YYYY-MM-DD format")
        public String date;
    }

    @JsonClassDescription(
            "Books an appointment for a patient with a specific doctor at a specific date and time. "
            + "IMPORTANT: Only call this AFTER the patient has explicitly confirmed the doctor, date, "
            + "and time (e.g., the patient said 'yes', 'ok', 'sure', 'book it')."
    )
    public static class BookAppointment {
        @JsonPropertyDescription("Full name of the doctor, e.g., 'Dr. Sharma'")
        public String doctorName;

        @JsonPropertyDescription("Appointment date in YYYY-MM-DD format")
        public String date;

        @JsonPropertyDescription("Appointment start time in HH:MM format, e.g., '10:00' or '14:30'")
        public String time;

        @JsonPropertyDescription("Full name of the patient")
        public String patientName;
    }
}
