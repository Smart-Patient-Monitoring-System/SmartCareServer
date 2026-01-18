package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor

public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // DB primary key

    @Column(nullable = false)
    private String name;

    private LocalDate dateOfBirth;

    private String address;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nicNo;

    private String gender;

    @Column(nullable = false)
    private String contactNo;

    @Column(nullable = false, unique = true)
    private String guardiansName;

    @Column(nullable = false)
    private String guardiansContactNo;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String bloodType;

    // Emergency panel fields
    @Column
    private String city;

    @Column
    private String district;

    @Column(name = "postal_code")
    private String postalCode;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "guardian_relationship")
    private String guardianRelationship;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(length = 1000)
    private String medicalConditions;

    @Column(length = 1000)
    private String allergies;

    @Column(length = 1000)
    private String currentMedications;

    @Column(name = "emergency_notes", length = 1000)
    private String emergencyNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Patient() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
