package com.example.IOT_service.service;

import com.example.IOT_service.dto.SensorDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, SensorDataDTO> kafkaTemplate;

    @Value("${kafka.topic.sensor-data}")
    private String sensorDataTopic;

    public void sendSensorData(SensorDataDTO sensorData) {
        log.info("Publishing sensor data to Kafka: {}", sensorData);

        Message<SensorDataDTO> message = MessageBuilder
                .withPayload(sensorData)
                .setHeader(KafkaHeaders.TOPIC, sensorDataTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(sensorData.getSensorId()))
                .build();

        kafkaTemplate.send(message);
    }
}