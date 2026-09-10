package com.clinic.assistant.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Represents a bookable time slot for a doctor.
 *
 * Each slot is a fixed block (e.g., 30 minutes).
 * When booked, isBooked is set to true so it won't appear in future availability queries.
 */
@Entity
@Table(name = "availability_slot")
public class AvailabilitySlot extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    public Doctor doctor;

    @Column(name = "slot_date", nullable = false)
    public LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    public LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    public LocalTime endTime;

    @Column(name = "is_booked", nullable = false)
    public boolean isBooked = false;

    // ---- Query methods ----

    /**
     * Find all AVAILABLE (unbooked) slots for a doctor on a given date.
     */
    public static List<AvailabilitySlot> findAvailable(Long doctorId, LocalDate date) {
        return find("doctor.id = ?1 AND slotDate = ?2 AND isBooked = false ORDER BY startTime",
                doctorId, date).list();
    }

    /**
     * Find a specific slot for booking: matches doctor, date, and start time.
     */
    public static AvailabilitySlot findSlot(Long doctorId, LocalDate date, LocalTime time) {
        return find("doctor.id = ?1 AND slotDate = ?2 AND startTime = ?3 AND isBooked = false",
                doctorId, date, time).firstResult();
    }
}
