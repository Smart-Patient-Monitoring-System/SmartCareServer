package com.example.mainservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    // Configuration for SMS Gateway (You can use Dialog, Mobitel, or Twilio)
    @Value("${sms.api.url:https://api.example.com/send}")
    private String smsApiUrl;

    @Value("${sms.api.key:your-api-key}")
    private String apiKey;

    // 1990 Suwa Sariya Emergency Number
    private static final String SUWA_SARIYA_NUMBER = "1990";

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send emergency alert to 1990 Suwa Sariya
     */
    public boolean sendEmergencyAlert(String patientName, String patientPhone,
                                      String address, Double latitude, Double longitude,
                                      String emergencyType) {
        try {
            String message = buildEmergencyMessage(patientName, patientPhone,
                    address, latitude, longitude, emergencyType);

            // Log the alert (In production, this would send actual SMS)
            System.out.println("=== EMERGENCY ALERT TO 1990 ===");
            System.out.println(message);
            System.out.println("===============================");

            // Uncomment below to send actual SMS via API
            // return sendSms(SUWA_SARIYA_NUMBER, message);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to send emergency alert: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send SMS to emergency contacts
     */
    public boolean notifyEmergencyContact(String contactPhone, String patientName,
                                          String alertType, String location) {
        try {
            String message = String.format(
                    "EMERGENCY ALERT: %s needs immediate assistance. " +
                            "Type: %s. Location: %s. Please contact them immediately.",
                    patientName, alertType, location
            );

            System.out.println("=== NOTIFYING CONTACT: " + contactPhone + " ===");
            System.out.println(message);
            System.out.println("===============================");

            // Uncomment to send actual SMS
            // return sendSms(contactPhone, message);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to notify contact: " + e.getMessage());
            return false;
        }
    }

    /**
     * Build emergency message for 1990
     */
    private String buildEmergencyMessage(String patientName, String patientPhone,
                                         String address, Double latitude, Double longitude,
                                         String emergencyType) {
        StringBuilder message = new StringBuilder();
        message.append("EMERGENCY: ").append(emergencyType).append("\n");
        message.append("Patient: ").append(patientName).append("\n");
        message.append("Phone: ").append(patientPhone).append("\n");
        message.append("Location: ").append(address).append("\n");

        if (latitude != null && longitude != null) {
            message.append("Coordinates: ").append(latitude).append(", ").append(longitude).append("\n");
            message.append("Google Maps: https://maps.google.com/?q=").append(latitude).append(",").append(longitude);
        }

        return message.toString();
    }

    /**
     * Send SMS via external API (Dialog, Mobitel, or Twilio)
     * Configure your SMS provider details in application.properties
     */
    private boolean sendSms(String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("to", phoneNumber);
            requestBody.put("message", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    smsApiUrl, request, String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("SMS API Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Alternative: Make direct phone call using Twilio Voice API
     */
    public boolean makeEmergencyCall(String toNumber, String message) {
        // This would use Twilio Voice API to make actual phone call
        System.out.println("=== INITIATING EMERGENCY CALL ===");
        System.out.println("Calling: " + toNumber);
        System.out.println("Message: " + message);
        System.out.println("================================");

        // TODO: Implement Twilio Voice API call
        return true;
    }
}