package com.example.IOT_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic sensorDataTopic() {
        return TopicBuilder.name("sensor-data-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sensorStatusTopic() {
        return TopicBuilder.name("sensor-status-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}