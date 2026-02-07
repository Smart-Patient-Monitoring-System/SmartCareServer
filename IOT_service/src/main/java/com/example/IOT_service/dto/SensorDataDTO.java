package com.example.IOT_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDTO {
    private Long sensorId;
    private String sensorName;
    private String sensorType;
    private Double value;
    private String unit;
    private String status;
    private LocalDateTime timestamp;
    private Long deviceId;
    private String deviceName;
}