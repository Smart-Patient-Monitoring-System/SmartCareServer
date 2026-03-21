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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor-notes")
@RequiredArgsConstructor
public class DoctorNotesController {

    private final DoctorNoteRepository doctorNoteRepository;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    @GetMapping("/patient/{patientId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getNotesByPatient(@PathVariable Long patientId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        List<Map<String, Object>> result = doctorNoteRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(note -> Map.<String, Object>of(
                        "id", note.getId(),
                        "content", note.getContent(),
                        "author", "Dr. " + note.getDoctor().getName(),
                        "createdAt", note.getCreatedAt().format(fmt)
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/patient/{patientId}")
    @Transactional
    public ResponseEntity<?> saveNote(
            @PathVariable Long patientId,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Note content cannot be empty"));
        }

        Doctor doctor = doctorRepo.findByUsername(principal.getName())
                .orElseGet(() -> doctorRepo.findByEmail(principal.getName()).orElse(null));
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only doctors can save notes"));
        }

        Patient patient = patientRepo.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Patient not found"));
        }

        DoctorNote saved = doctorNoteRepository.save(DoctorNote.builder()
                .content(content.trim())
                .doctor(doctor)
                .patient(patient)
                .build());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "content", saved.getContent(),
                "author", "Dr. " + doctor.getName(),
                "createdAt", saved.getCreatedAt().format(fmt)
        ));
    }
}
