package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_sessions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String name;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "upload_date", nullable = false)
    private LocalDate uploadDate;

    @Column(nullable = false)
    private String source; // Apple Health, Google Fit, Custom Health Data

    @Lob
    @Column(name = "health_data_json", columnDefinition = "LONGTEXT")
    private String healthData; // JSON string of health data

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (uploadDate == null) {
            uploadDate = LocalDate.now();
        }
        if (workoutDate == null) {
            workoutDate = uploadDate;
        }
        if (sessionName == null && name != null) {
            sessionName = name;
        }
        if (name == null && sessionName != null) {
            name = sessionName;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (sessionName == null && name != null) {
            sessionName = name;
        }
        if (workoutDate == null && uploadDate != null) {
            workoutDate = uploadDate;
        }
    }
}
