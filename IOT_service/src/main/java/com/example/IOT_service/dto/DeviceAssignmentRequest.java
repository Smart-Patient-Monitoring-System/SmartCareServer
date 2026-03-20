package com.example.IOT_service.dto;

import lombok.Data;

@Data
public class DeviceAssignmentRequest {
    private String deviceId;
    private Long userId;
}