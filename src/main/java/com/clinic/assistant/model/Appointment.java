package com.clinic.assistant.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Records a booked appointment.
 *
 * Created when book_appointment is called successfully.
 * Stores patient details and the booking timestamp for reference.
 */
@Entity
@Table(name = "appointment")
public class Appointment extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    public AvailabilitySlot slot;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    public Doctor doctor;

    @Column(name = "patient_name", nullable = false)
    public String patientName;

    @Column(name = "patient_phone")
    public String patientPhone;

    @Column(name = "appointment_date", nullable = false)
    public LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    public LocalTime appointmentTime;

    @Column(name = "booked_at", nullable = false)
    public LocalDateTime bookedAt;
}
