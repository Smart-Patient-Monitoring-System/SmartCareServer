package com.example.mainservice.controller;

import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.DoctorNote;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.DoctorNoteRepository;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/doctor-notes")
@RequiredArgsConstructor
public class DoctorNotesController {

    private final DoctorNoteRepository doctorNoteRepository;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    /**
     * Get all notes for a patient
     */
    @GetMapping("/patient/{patientId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getNotesByPatient(@PathVariable Long patientId) {
        List<DoctorNote> notes = doctorNoteRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

        List<Map<String, Object>> result = notes.stream().map(n -> Map.<String, Object>of(
            "id", n.getId(),
            "content", n.getContent(),
            "author", "Dr. " + n.getDoctor().getName(),
            "createdAt", n.getCreatedAt().format(fmt)
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Save a new note for a patient (doctor only)
     */
    @PostMapping("/patient/{patientId}")
    @Transactional
    public ResponseEntity<?> saveNote(
            @PathVariable Long patientId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        String principalName = principal.getName();
        System.out.println("[DOCTOR-NOTES] saveNote called by principal: " + principalName);

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Note content cannot be empty"));
        }

        // Try by username first, then by email
        Doctor doctor = doctorRepo.findByUsername(principalName).orElse(null);
        if (doctor == null) {
            doctor = doctorRepo.findByEmail(principalName).orElse(null);
        }
        System.out.println("[DOCTOR-NOTES] Doctor lookup result: " + (doctor != null ? doctor.getName() : "NOT FOUND"));
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only doctors can save notes"));
        }

        Patient patient = patientRepo.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Patient not found"));
        }

        DoctorNote note = DoctorNote.builder()
                .content(content.trim())
                .doctor(doctor)
                .patient(patient)
                .build();

        DoctorNote saved = doctorNoteRepository.save(note);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

        return ResponseEntity.ok(Map.of(
            "id", saved.getId(),
            "content", saved.getContent(),
            "author", "Dr. " + doctor.getName(),
            "createdAt", saved.getCreatedAt().format(fmt)
        ));
    }
}
