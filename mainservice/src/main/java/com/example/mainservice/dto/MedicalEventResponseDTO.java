package com.example.mainservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class MedicalEventResponseDTO {

    private Long id;
    private String type;
    private LocalDateTime recordedAt;
    private Map<String, Object> payload;
    private String createdBy;

    public MedicalEventResponseDTO(Long id, String type, LocalDateTime recordedAt, Map<String, Object> payload, String createdBy) {
        this.id = id;
        this.type = type;
        this.recordedAt = recordedAt;
        this.payload = payload;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public Map<String, Object> getPayload() { return payload; }
    public String getCreatedBy() { return createdBy; }
}
