package com.example.mainservice.dto;

import java.util.Map;

public class MedicalEventCreateDTO {
    private String type;           // "VITALS", ...
    private String recordedAt;     // "2026-02-04T10:30:00"
    private Map<String, Object> payload;

    public MedicalEventCreateDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
