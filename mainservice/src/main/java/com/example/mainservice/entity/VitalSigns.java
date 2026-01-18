// File: src/main/java/com/example/mainservice/entity/VitalSigns.java

package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vital_signs")
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Blood Pressure
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;

    // Blood Sugar
    private Double bloodSugar;

    // Temperature
    private Double temperature;

    // Heart Rate
    private Integer heartRate;

    // SpO2
    private Integer spo2;

    // Weight
    private Double weight;

    // Date and Time
    private LocalDateTime measurementDateTime;

    // Notes
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Patient identifier (you can link to User entity if needed)
    private Long patientId;

    // Metadata
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}