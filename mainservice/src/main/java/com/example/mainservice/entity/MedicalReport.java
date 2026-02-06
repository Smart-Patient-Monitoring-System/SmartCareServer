package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medical_reports")
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(length = 20)
    private String ocrStatus; // PENDING, DONE, FAILED

    @Column(length = 255)
    private String ocrError;

    public MedicalReport(String reportName,
                         String fileName,
                         String fileType,
                         Long fileSize,
                         Patient patient) {
        this.reportName = reportName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
        this.patient = patient;
    }
}
