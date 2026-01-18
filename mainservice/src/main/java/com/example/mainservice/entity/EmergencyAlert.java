package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "alert_type", nullable = false)
    private String alertType; // MEDICAL, ACCIDENT, FIRE, etc.

    @Column(nullable = false)
    private String status; // ACTIVE, RESOLVED, CANCELLED

    @Column
    private String description;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "nearest_hospital_id")
    private Long nearestHospitalId;

    @Column(name = "ambulance_called")
    private Boolean ambulanceCalled = false;

    @Column(name = "ambulance_eta")
    private Integer ambulanceEta; // in minutes

    @Column(name = "contacts_notified")
    private Boolean contactsNotified = false;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private java.time.LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
