package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.SpecialDoctor;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.SpecialDoctorRepository;
import com.example.mainservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;
    private final SpecialDoctorRepository specialDoctorRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * GET /api/doctor/appointments/{doctorId}
     * Get all paid appointments for a specific SpecialDoctor by ID.
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    /**
     * GET /api/doctor/appointments/by-email?email=...
     * Get appointments for a doctor by their email address.
     * Used by doctor portal when doctor is identified by login email.
     */
    @GetMapping("/by-email")
    public ResponseEntity<?> getDoctorAppointmentsByEmail(@RequestParam String email) {
        SpecialDoctor doctor = specialDoctorRepository.findByEmail(email).orElse(null);
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctor.getId()));
    }

    /**
     * PUT /api/doctor/appointments/{appointmentId}/link
     * Doctor sets the zoom/meeting link for an online appointment.
     * Body: { "link": "https://zoom.us/j/..." }
     */
    @PutMapping("/{appointmentId}/link")
    public ResponseEntity<?> setMeetingLink(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String link = body.get("link");
            if (link == null || link.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Link cannot be empty"));
            }
            return ResponseEntity.ok(appointmentService.setMeetingLink(appointmentId, link));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/doctor/appointments/{appointmentId}/confirm
     * Doctor confirms the appointment (changes status to CONFIRMED).
     */
    @PutMapping("/{appointmentId}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long appointmentId) {
        try {
            return ResponseEntity.ok(appointmentService.confirmAppointment(appointmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/doctor/appointments/confirm/{id}
     * Alternate confirm endpoint (POST style) that also sets location/zoom link.
     * Supports both doctor and admin portal confirm flows.
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirmWithDetails(
            @PathVariable Long id,
            @RequestParam(required = false) String physicalLocation,
            @RequestParam(required = false) String zoomLink
    ) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if ("Physical".equalsIgnoreCase(a.getAppointmentType().getTypeName())) {
            if (physicalLocation != null && !physicalLocation.isBlank()) {
                a.setPhysicalLocation(physicalLocation);
            }
        } else {
            if (zoomLink != null && !zoomLink.isBlank()) {
                a.setOnlineLink(zoomLink);
            }
        }

        a.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(a);
        return ResponseEntity.ok("Confirmed");
    }
}
