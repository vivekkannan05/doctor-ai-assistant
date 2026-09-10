package com.clinic.assistant.service;

import com.clinic.assistant.model.Appointment;
import com.clinic.assistant.model.AvailabilitySlot;
import com.clinic.assistant.model.Doctor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic layer — the 6 tool functions that query/update the database.
 *
 * This class knows NOTHING about OpenAI. It's pure clinic operations.
 * The chatbot layer calls these methods when the LLM makes tool calls.
 *
 * @ApplicationScoped = Quarkus creates ONE instance, shared across the app (singleton).
 */
@ApplicationScoped
@Transactional
public class ClinicService {

    /**
     * TOOL 1: Get all doctors at the clinic.
     */
    public List<Map<String, Object>> getAllDoctors() {
        return Doctor.listAll().stream()
                .map(entity -> {
                    Doctor d = (Doctor) entity;
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", d.name);
                    map.put("specialty", d.specialty);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * TOOL 2: Find a doctor by name (fuzzy match).
     * Returns matching doctors or an error if none found.
     */
    public Map<String, Object> getDoctorByName(String name) {
        List<Doctor> matches = Doctor.findByNameContaining(name);
        if (matches.isEmpty()) {
            return Map.of("error", "No doctor found matching '" + name + "'");
        }
        Doctor d = matches.get(0);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", d.name);
        map.put("specialty", d.specialty);
        return map;
    }

    /**
     * TOOL 3: Get all distinct specialties available at the clinic.
     */
    public List<String> getAllSpecialties() {
        return Doctor.listAll().stream()
                .map(entity -> ((Doctor) entity).specialty)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * TOOL 4: Get all doctors for a given specialty.
     * Returns matching doctors or a message if the specialty isn't available.
     */
    public Map<String, Object> getDoctorsBySpecialty(String specialty) {
        List<Doctor> doctors = Doctor.findBySpecialty(specialty);
        if (doctors.isEmpty()) {
            return Map.of(
                    "available", false,
                    "message", "Sorry, we do not have any doctors specializing in "
                            + specialty + " at our clinic."
            );
        }
        List<String> names = doctors.stream().map(d -> d.name).collect(Collectors.toList());
        return Map.of(
                "available", true,
                "specialty", specialty,
                "doctors", names
        );
    }

    /**
     * TOOL 5: Get available time slots for a doctor on a specific date.
     * Returns open slots or a message if none available.
     */
    public Map<String, Object> getAvailabilities(String doctorName, String dateStr) {
        // Find the doctor (fuzzy match)
        List<Doctor> matches = Doctor.findByNameContaining(doctorName);
        if (matches.isEmpty()) {
            return Map.of("error", "No doctor found matching '" + doctorName + "'");
        }
        Doctor doctor = matches.get(0);

        // Parse the date
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return Map.of("error", "Invalid date format '" + dateStr + "'. Use YYYY-MM-DD.");
        }

        // Query available slots
        List<AvailabilitySlot> slots = AvailabilitySlot.findAvailable(doctor.id, date);
        if (slots.isEmpty()) {
            return Map.of(
                    "doctor", doctor.name,
                    "date", dateStr,
                    "available", false,
                    "message", doctor.name + " has no available slots on " + dateStr
            );
        }

        List<String> times = slots.stream()
                .map(s -> s.startTime.toString() + " - " + s.endTime.toString())
                .collect(Collectors.toList());

        return Map.of(
                "doctor", doctor.name,
                "date", dateStr,
                "available", true,
                "slots", times
        );
    }

    /**
     * TOOL 6: Book an appointment.
     *
     * Two-step DB operation:
     *   1. Mark the availability slot as booked (is_booked = true)
     *   2. Create an appointment record with patient details
     *
     * @Transactional ensures both steps succeed or both fail (atomicity).
     */
    public Map<String, Object> bookAppointment(String doctorName, String dateStr,
                                                String timeStr, String patientName) {
        // Find the doctor
        List<Doctor> matches = Doctor.findByNameContaining(doctorName);
        if (matches.isEmpty()) {
            return Map.of("error", "No doctor found matching '" + doctorName + "'");
        }
        Doctor doctor = matches.get(0);

        // Parse date and time
        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateStr);
            time = LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            return Map.of("error", "Invalid date/time format. Use YYYY-MM-DD and HH:MM.");
        }

        // Find the matching available slot
        AvailabilitySlot slot = AvailabilitySlot.findSlot(doctor.id, date, time);
        if (slot == null) {
            return Map.of(
                    "error", doctor.name + " does not have an available slot at "
                            + timeStr + " on " + dateStr
            );
        }

        // Step 1: Mark slot as booked
        slot.isBooked = true;
        slot.persist();

        // Step 2: Create appointment record
        Appointment appointment = new Appointment();
        appointment.slot = slot;
        appointment.doctor = doctor;
        appointment.patientName = patientName;
        appointment.appointmentDate = date;
        appointment.appointmentTime = time;
        appointment.bookedAt = LocalDateTime.now();
        appointment.persist();

        return Map.of(
                "status", "confirmed",
                "doctor", doctor.name,
                "date", dateStr,
                "time", timeStr,
                "patient", patientName,
                "message", "Appointment confirmed for " + patientName + " with "
                        + doctor.name + " on " + dateStr + " at " + timeStr
        );
    }
}
