package com.clinic.assistant.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;

/**
 * Represents a doctor at Super Clinic.
 *
 * Extends PanacheEntity which provides:
 *   - Auto-generated 'id' field (Long)
 *   - Built-in methods: listAll(), find(), persist(), delete()
 *   - No need to write a separate Repository class for simple queries
 */
@Entity
@Table(name = "doctor")
public class Doctor extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String specialty;

    // ---- Query methods (Panache makes this simple) ----

    /**
     * Find all doctors matching a given specialty (case-insensitive).
     * Panache translates this to: SELECT * FROM doctor WHERE LOWER(specialty) = LOWER(?)
     */
    public static List<Doctor> findBySpecialty(String specialty) {
        return find("LOWER(specialty) = LOWER(?1)", specialty).list();
    }

    /**
     * Find a doctor by name (case-insensitive, partial match).
     * e.g., "Sharma" matches "Dr. Sharma"
     */
    public static List<Doctor> findByNameContaining(String name) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + name + "%").list();
    }

    /**
     * Get all distinct specialties available at the clinic.
     */
    public static List<String> findAllSpecialties() {
        return find("SELECT DISTINCT d.specialty FROM Doctor d").project(String.class).list();
    }
}
