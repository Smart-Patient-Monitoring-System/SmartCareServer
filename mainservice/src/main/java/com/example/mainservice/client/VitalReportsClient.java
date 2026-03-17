package com.example.mainservice.client;

import com.example.mainservice.dto.VitalAssessmentResponseDTO;
import com.example.mainservice.dto.VitalReadingRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class VitalReportsClient {

    private static final Logger logger = LoggerFactory.getLogger(VitalReportsClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vitalreports.url:http://localhost:8081}")
    private String vitalReportsUrl;

    public VitalAssessmentResponseDTO evaluateVitals(VitalReadingRequestDTO request) {
        String url = vitalReportsUrl + "/api/vital/vitals/evaluate";
        try {
            logger.info("Calling VitalReports-AI at {}", url);
            ResponseEntity<VitalAssessmentResponseDTO> response =
                    restTemplate.postForEntity(url, request, VitalAssessmentResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to evaluate vitals via VitalReports-AI: {}", e.getMessage());
            VitalAssessmentResponseDTO fallback = new VitalAssessmentResponseDTO();
            fallback.setTriageLevel("UNKNOWN");
            fallback.setVitalStatus(new VitalAssessmentResponseDTO.VitalStatusDTO(
                    "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN"
            ));
            return fallback;
        }
    }
}
