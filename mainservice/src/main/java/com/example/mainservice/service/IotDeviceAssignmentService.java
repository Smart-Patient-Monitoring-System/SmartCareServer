package com.example.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IotDeviceAssignmentService {

    @Value("${iot.service.url:http://iot-service:8082}")
    private String iotServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void assignDevice(String deviceId, Long userId) {
        String url = iotServiceUrl + "/api/sensordata/devices/assign";

        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", deviceId);
        body.put("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
    }
}