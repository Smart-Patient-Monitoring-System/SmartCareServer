package com.example.IOT_service.service;

import com.example.IOT_service.dto.SensorDataDTO;
import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private final SensorDataRepository repository;

    @Transactional
    public SensorData saveSensorData(SensorData data) {
        // Save to database
        SensorData saved = repository.save(data);

        // Publish to Kafka for mainservice
        publishToKafka(saved);

        return saved;
    }

    private void publishToKafka(SensorData sensorData) {
        try {
            SensorDataDTO dto = new SensorDataDTO();

            // Map all sensor readings to DTO
            dto.setSensorId(sensorData.getId());
            dto.setTimestamp(sensorData.getReceivedAt());

            // Since you have multiple sensor types in one record,
            // you can either send multiple messages or combine them
            // Option 1: Send as combined sensor data
            dto.setSensorName("ESP32_Sensors");
            dto.setSensorType("MULTI_SENSOR");

            // You can create a JSON string with all values or send separately
            // For now, let's create a structured approach

            // Send Room Temperature
            if (sensorData.getRoomTemp() != null) {
                SensorDataDTO tempDTO = createDTO(sensorData, "Room_Temperature",
                        sensorData.getRoomTemp(), "°C", "TEMPERATURE");
                kafkaProducerService.sendSensorData(tempDTO);
            }

            // Send Humidity
            if (sensorData.getHumidity() != null) {
                SensorDataDTO humidityDTO = createDTO(sensorData, "Humidity",
                        sensorData.getHumidity(), "%", "HUMIDITY");
                kafkaProducerService.sendSensorData(humidityDTO);
            }

            // Send Water Temperature
            if (sensorData.getWaterTempC() != null) {
                SensorDataDTO waterTempDTO = createDTO(sensorData, "Water_Temperature",
                        sensorData.getWaterTempC(), "°C", "TEMPERATURE");
                kafkaProducerService.sendSensorData(waterTempDTO);
            }

            // Send Heart Rate (BPM)
            if (sensorData.getBpm() != null) {
                SensorDataDTO bpmDTO = createDTO(sensorData, "Heart_Rate",
                        sensorData.getBpm(), "BPM", "HEART_RATE");
                kafkaProducerService.sendSensorData(bpmDTO);
            }

            // Send SpO2
            if (sensorData.getSpo2() != null) {
                SensorDataDTO spo2DTO = createDTO(sensorData, "SpO2",
                        sensorData.getSpo2().doubleValue(), "%", "OXYGEN_SATURATION");
                kafkaProducerService.sendSensorData(spo2DTO);
            }

        } catch (Exception e) {
            // Log error but don't fail the save operation
            System.err.println("Error publishing to Kafka: " + e.getMessage());
        }
    }

    private SensorDataDTO createDTO(SensorData sensorData, String name, Double value,
                                    String unit, String type) {
        SensorDataDTO dto = new SensorDataDTO();
        dto.setSensorId(sensorData.getId());
        dto.setSensorName(name);
        dto.setSensorType(type);
        dto.setValue(value);
        dto.setUnit(unit);
        dto.setStatus(determineStatus(value, type));
        dto.setTimestamp(sensorData.getReceivedAt());
        dto.setDeviceId(1L); // ESP32 device ID
        dto.setDeviceName("ESP32_Patient_Monitor");
        return dto;
    }

    private String determineStatus(Double value, String type) {
        // Add logic to determine if readings are normal/abnormal
        switch (type) {
            case "HEART_RATE":
                return (value >= 60 && value <= 100) ? "NORMAL" : "ABNORMAL";
            case "OXYGEN_SATURATION":
                return (value >= 95) ? "NORMAL" : "LOW";
            case "TEMPERATURE":
                return (value >= 36 && value <= 37.5) ? "NORMAL" : "ABNORMAL";
            default:
                return "NORMAL";
        }
    }

    public List<SensorData> getAllData() {
        return repository.findAllByOrderByReceivedAtDesc();
    }

    public List<SensorData> getLatestData(int limit) {
        return repository.findLatestN(limit);
    }

    public Optional<SensorData> getLatestReading() {
        return repository.findTopByOrderByReceivedAtDesc();
    }

    public List<SensorData> getDataInRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByReceivedAtBetweenOrderByReceivedAtDesc(start, end);
    }

    public List<SensorData> getRecentData(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findRecentData(since);
    }

    @Transactional
    public void deleteOldData(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        List<SensorData> oldData = repository.findByReceivedAtBetweenOrderByReceivedAtDesc(
                LocalDateTime.MIN, cutoff
        );
        repository.deleteAll(oldData);
    }
}