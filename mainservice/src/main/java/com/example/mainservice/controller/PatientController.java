package com.example.mainservice.controller;

import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.EmergencyContact;
import com.example.mainservice.entity.Hospital;
import com.example.mainservice.service.PatientService;
import com.example.mainservice.service.EmergencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/api/patient")
public class PatientController {
    @Autowired
    private PatientService patientservice;

    @Autowired
    private EmergencyService emergencyService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody PatientDTO patientDto) {
        Patient patient = patientservice.create(patientDto);

        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        response.put("message", "Patient registered successfully");
        response.put("emergencyContactCreated",
                patient.getGuardiansName() != null && patient.getGuardiansContactNo() != null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get")
    public List<PatientDTO> getAllPatients() {
        return patientservice.getDetails();
    }

    @DeleteMapping("/delete/{Id}")
    public String deletePatientByID(@PathVariable Long Id) {
        try {
            patientservice.deletePatient(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }

    @PutMapping("/update/{Id}")
    public ResponseEntity<PatientDTO> updatePatientByID(
            @PathVariable Long Id,
            @RequestBody PatientDTO patientDto) {
        try {
            PatientDTO updatedPatient = patientservice.updatePatient(Id, patientDto);
            return ResponseEntity.ok(updatedPatient);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // NEW: Emergency Panel Endpoint
    @GetMapping("/emergency-panel/{patientId}")
    public ResponseEntity<Map<String, Object>> getEmergencyPanelData(
            @PathVariable Long patientId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        try {
            Patient patient = patientservice.getPatientByUsername(
                    patientservice.getDetails().stream()
                            .filter(p -> p.getId().equals(patientId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Patient not found"))
                            .getUsername()
            );

            Map<String, Object> panelData = new HashMap<>();

            // Get emergency contacts
            List<EmergencyContact> contacts = emergencyService.getUserContacts(patientId);
            panelData.put("emergencyContacts", contacts);

            // Use patient's stored location if coordinates not provided
            if (latitude == null && longitude == null) {
                latitude = patient.getLatitude();
                longitude = patient.getLongitude();
            }

            // Get nearest hospital if location available
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
            var activeAlert = emergencyService.getActiveAlert(patientId);
            panelData.put("activeAlert", activeAlert);

            // Add patient info
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("name", patient.getName());
            patientInfo.put("bloodType", patient.getBloodType());
            patientInfo.put("allergies", patient.getAllergies());
            patientInfo.put("medicalConditions", patient.getMedicalConditions());
            patientInfo.put("currentMedications", patient.getCurrentMedications());
            panelData.put("patientInfo", patientInfo);

            return ResponseEntity.ok(panelData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Get patient by ID endpoint
    @GetMapping("/get/{Id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long Id) {
        try {
            PatientDTO patient = patientservice.getDetails().stream()
                    .filter(p -> p.getId().equals(Id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}