package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorAssignmentItemDTO;
import com.example.mainservice.dto.DoctorAssignmentRequestDTO;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.service.DoctorAssignmentService;
import com.example.mainservice.service.DoctorService;
import com.example.mainservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor-assignments")
@RequiredArgsConstructor
public class DoctorAssignmentAdminController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DoctorAssignmentService doctorAssignmentService;

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> getDoctors() {
        return ResponseEntity.ok(doctorService.getDetails());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getPatients() {
        return ResponseEntity.ok(patientService.getDetails());
    }

    @GetMapping
    public ResponseEntity<List<DoctorAssignmentItemDTO>> getAssignments() {
        return ResponseEntity.ok(doctorAssignmentService.getAllAssignments());
    }

    @PutMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignDoctor(@RequestBody DoctorAssignmentRequestDTO request) {
        doctorAssignmentService.assignDoctorToPatient(request.getDoctorId(), request.getPatientId());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor assigned successfully");
        response.put("doctorId", request.getDoctorId());
        response.put("patientId", request.getPatientId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unassign/{patientId}")
    public ResponseEntity<Map<String, Object>> unassignDoctor(@PathVariable Long patientId) {
        doctorAssignmentService.unassignDoctorFromPatient(patientId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor unassigned successfully");
        response.put("patientId", patientId);
        return ResponseEntity.ok(response);
    }
}
