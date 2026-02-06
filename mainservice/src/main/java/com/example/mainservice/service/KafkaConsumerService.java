package com.example.mainservice.service;

import com.example.mainservice.dto.SensorDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class KafkaConsumerService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${kafka.topic.sensor-data}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSensorData(@Payload SensorDataDTO sensorData) {
        log.info("Received sensor data from Kafka: {} = {} {}",
                sensorData.getSensorName(),
                sensorData.getValue(),
                sensorData.getUnit());

        // Process the sensor data
        processSensorData(sensorData);

        // Send to WebSocket for real-time updates (since you have WebSocket configured)
        sendToWebSocket(sensorData);
    }

    private void processSensorData(SensorDataDTO sensorData) {
        // Check for alerts
        if ("ABNORMAL".equals(sensorData.getStatus())) {
            log.warn("ALERT: Abnormal reading detected - {} = {} {}",
                    sensorData.getSensorName(),
                    sensorData.getValue(),
                    sensorData.getUnit());

            // Trigger alert logic here
            triggerAlert(sensorData);
        }

        // Store in mainservice database if needed
        // saveToDatabaseIfNeeded(sensorData);

        // Update analytics/dashboards
        // updateAnalytics(sensorData);
    }

    private void sendToWebSocket(SensorDataDTO sensorData) {
        try {
            // Send to WebSocket topic for real-time frontend updates
            messagingTemplate.convertAndSend("/topic/sensor-data", sensorData);
            log.info("Sent sensor data to WebSocket: {}", sensorData.getSensorName());
        } catch (Exception e) {
            log.error("Error sending to WebSocket: {}", e.getMessage());
        }
    }

    private void triggerAlert(SensorDataDTO sensorData) {
        // Create alert notification
        // You can integrate with your existing notification system
        log.info("Triggering alert for: {}", sensorData.getSensorName());

        // Send alert via WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", Map.of(
                "type", "SENSOR_ALERT",
                "sensor", sensorData.getSensorName(),
                "value", sensorData.getValue(),
                "status", sensorData.getStatus(),
                "timestamp", sensorData.getTimestamp()
        ));
    }
}