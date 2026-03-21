package com.example.mainservice.controller;

import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/patient")
@RequiredArgsConstructor
public class AdminPatientController {

    private final PatientService patientService;

    @PostMapping("/create")
    public ResponseEntity<?> createPatient(@RequestBody PatientDTO patientDto) {
        return ResponseEntity.ok(patientService.create(patientDto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody PatientDTO patientDto) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDto));
    }
}