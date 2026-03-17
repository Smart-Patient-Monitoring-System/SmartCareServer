package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ECGReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String prediction;
    private double probability;
    private int meanHR;
    private double sdnn;
    private double rmssd;
    private int beats;
    private String status;

    @Column(columnDefinition = "LONGTEXT")
    private String rationale;

    @Column(columnDefinition = "LONGTEXT")
    private String waveformJson;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}
