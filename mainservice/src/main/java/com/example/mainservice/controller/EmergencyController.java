package com.example.mainservice.controller;

import com.example.mainservice.entity.EmergencyAlert;
import com.example.mainservice.entity.EmergencyContact;
import com.example.mainservice.entity.Hospital;
import com.example.mainservice.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyService emergencyService;

    // Emergency Contact Endpoints
    @GetMapping("/contacts/{userId}")
    public ResponseEntity<List<EmergencyContact>> getUserContacts(@PathVariable Long userId) {
        return ResponseEntity.ok(emergencyService.getUserContacts(userId));
    }

    @PostMapping("/contacts")
    public ResponseEntity<EmergencyContact> addContact(@Valid @RequestBody EmergencyContact contact) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emergencyService.addContact(contact));
    }

    @PutMapping("/contacts/{userId}/{contactId}")
    public ResponseEntity<EmergencyContact> updateContact(
            @PathVariable Long userId,
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContact contact) {
        return ResponseEntity.ok(emergencyService.updateContact(userId, contactId, contact));
    }

    @DeleteMapping("/contacts/{userId}/{contactId}")
    public ResponseEntity<Map<String, String>> deleteContact(
            @PathVariable Long userId,
            @PathVariable Long contactId) {
        emergencyService.deleteContact(userId, contactId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contact deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Hospital Endpoints
    @GetMapping("/hospitals/nearest")
    public ResponseEntity<Map<String, Object>> getNearestHospitals(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Integer limit) {

        List<Hospital> hospitals = emergencyService.getNearestHospitals(latitude, longitude, limit);

        Map<String, Object> response = new HashMap<>();
        if (!hospitals.isEmpty()) {
            Hospital nearest = hospitals.get(0);
            Integer eta = emergencyService.calculateETA(latitude, longitude,
                    nearest.getLatitude(), nearest.getLongitude());

            response.put("nearestHospital", nearest);
            response.put("eta", eta);
            response.put("allNearbyHospitals", hospitals);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(emergencyService.getAllHospitals());
    }

    @PostMapping("/hospitals")
    public ResponseEntity<Hospital> addHospital(@Valid @RequestBody Hospital hospital) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emergencyService.addHospital(hospital));
    }

    // Emergency Alert Endpoints
    @PostMapping("/alert/{userId}")
    public ResponseEntity<Map<String, Object>> createAlert(
            @PathVariable Long userId,
            @Valid @RequestBody EmergencyAlert alert) {
        EmergencyAlert createdAlert = emergencyService.createEmergencyAlert(userId, alert);

        Map<String, Object> response = new HashMap<>();
        response.put("alert", createdAlert);
        response.put("message", "Emergency alert created successfully");
        response.put("ambulanceCalled", createdAlert.getAmbulanceCalled());
        response.put("contactsNotified", createdAlert.getContactsNotified());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/alert/{userId}")
    public ResponseEntity<List<EmergencyAlert>> getUserAlerts(@PathVariable Long userId) {
        return ResponseEntity.ok(emergencyService.getUserAlerts(userId));
    }

    @GetMapping("/alert/{userId}/active")
    public ResponseEntity<EmergencyAlert> getActiveAlert(@PathVariable Long userId) {
        EmergencyAlert alert = emergencyService.getActiveAlert(userId);
        if (alert == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alert);
    }

    @PutMapping("/alert/{alertId}/status")
    public ResponseEntity<EmergencyAlert> updateAlertStatus(
            @PathVariable Long alertId,
            @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        return ResponseEntity.ok(emergencyService.updateAlertStatus(alertId, status));
    }

    // Emergency Panel Data Endpoint (Combined data for frontend)
    @GetMapping("/panel/{userId}")
    public ResponseEntity<Map<String, Object>> getEmergencyPanelData(
            @PathVariable Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        Map<String, Object> panelData = new HashMap<>();

        // Get emergency contacts
        List<EmergencyContact> contacts = emergencyService.getUserContacts(userId);
        panelData.put("emergencyContacts", contacts);

        // Get nearest hospital if location provided
        if (latitude != null && longitude != null) {
            Hospital nearestHospital = emergencyService.getNearestHospital(latitude, longitude);
            if (nearestHospital != null) {
                Integer eta = emergencyService.calculateETA(latitude, longitude,
                        nearestHospital.getLatitude(), nearestHospital.getLongitude());

                Map<String, Object> hospitalData = new HashMap<>();
                hospitalData.put("hospital", nearestHospital);
                hospitalData.put("distance", String.format("%.1f km",
                        emergencyService.calculateDistance(latitude, longitude,
                                nearestHospital.getLatitude(), nearestHospital.getLongitude())));
                hospitalData.put("eta", eta + " mins");

                panelData.put("nearestHospital", hospitalData);
            }
        }

        // Get active alert if any
        EmergencyAlert activeAlert = emergencyService.getActiveAlert(userId);
        panelData.put("activeAlert", activeAlert);

        return ResponseEntity.ok(panelData);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Emergency Panel API");
        return ResponseEntity.ok(response);
    }
}