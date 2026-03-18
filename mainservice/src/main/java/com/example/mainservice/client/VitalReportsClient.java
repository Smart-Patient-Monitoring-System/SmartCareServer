package com.example.mainservice.client;

import com.example.mainservice.dto.VitalAssessmentResponseDTO;
import com.example.mainservice.dto.VitalReadingRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import java.util.Map;

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

    public Map<String, Object> analyzeECG(String patientId, MultipartFile datFile, MultipartFile heaFile) {
        String url = vitalReportsUrl + "/api/vital/ecg/analyze";
        try {
            logger.info("Sending ECG data to VitalReports-AI at {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("patientId", patientId);
            body.add("dat_file", datFile.getResource());
            body.add("hea_file", heaFile.getResource());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to analyze ECG via VitalReports-AI: {}", e.getMessage());
            throw new RuntimeException("ECG Analysis failed in VitalReports service: " + e.getMessage());
        }
    }
}
