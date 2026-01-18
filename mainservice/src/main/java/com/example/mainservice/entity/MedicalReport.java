package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
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

    public MedicalReport(String reportName,
                         String fileName,
                         String fileType,
                         Long fileSize) {
        this.reportName = reportName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
    }
}
