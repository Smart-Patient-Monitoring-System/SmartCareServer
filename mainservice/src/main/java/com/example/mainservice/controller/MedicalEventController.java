package com.example.mainservice.controller;

import com.example.mainservice.dto.MedicalEventCreateDTO;
import com.example.mainservice.dto.MedicalEventResponseDTO;
import com.example.mainservice.dto.MedicalSummaryDTO;
import com.example.mainservice.service.MedicalEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}")
public class MedicalEventController {

    private final MedicalEventService service;

    public MedicalEventController(MedicalEventService service) {
        this.service = service;
    }

    // ✅ React: POST /api/patients/{patientId}/medical-events
    @PostMapping("/medical-events")
    public ResponseEntity<?> addEvent(@PathVariable Long patientId, @RequestBody MedicalEventCreateDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = (auth != null) ? auth.getName() : null;

        service.addEvent(patientId, dto, createdBy);
        return ResponseEntity.ok().build();
    }

    // ✅ React: GET /api/patients/{patientId}/medical-events?from=...&to=...
    @GetMapping("/medical-events")
    public ResponseEntity<List<MedicalEventResponseDTO>> getEvents(
            @PathVariable Long patientId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(service.getEvents(patientId, from, to));
    }

    //  React: GET /api/patients/{patientId}/medical-summary
    @GetMapping("/medical-summary")
    public ResponseEntity<MedicalSummaryDTO> summary(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getSummary(patientId));
    }
}
