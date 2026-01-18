package com.example.mainservice.dto;

import java.time.LocalDateTime;

public class ReportResponseDTO {

    private Long id;
    private String reportName;
    private LocalDateTime uploadedAt;

    public ReportResponseDTO(Long id, String reportName, LocalDateTime uploadedAt) {
        this.id = id;
        this.reportName = reportName;
        this.uploadedAt = uploadedAt;
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
}
