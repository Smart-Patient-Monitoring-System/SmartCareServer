package com.example.mainservice.dto;

import java.time.LocalDateTime;

public class ReportResponseDTO {

    private Long id;
    private String reportName;
    private LocalDateTime uploadedAt;
    private Long patientId;
    private String patientName;

    public ReportResponseDTO(Long id, String reportName, LocalDateTime uploadedAt) {
        this.id = id;
        this.reportName = reportName;
        this.uploadedAt = uploadedAt;
    }

    public ReportResponseDTO(Long id, String reportName, LocalDateTime uploadedAt, Long patientId, String patientName) {
        this.id = id;
        this.reportName = reportName;
        this.uploadedAt = uploadedAt;
        this.patientId = patientId;
        this.patientName = patientName;
    }

    public Long getId() {
        return id;
    }

    public String getReportName() {
        return reportName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }
}
