package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.security.CustomUserDetails;
import com.example.mainservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * POST /api/appointments/book
     * Patient books an appointment. Requires PATIENT or ADMIN role.
     * Returns appointment with PENDING payment status so frontend can redirect to PayHere.
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestBody AppointmentRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Principal principal
    ) {
        try {
            // Use display name if available, else fall back to principal name
            String patientName = (userDetails != null && userDetails.getDisplayName() != null)
                    ? userDetails.getDisplayName()
                    : (principal != null ? principal.getName() : "Unknown");

            Long patientId = (userDetails != null) ? userDetails.getId() : null;

            return ResponseEntity.ok(appointmentService.bookAppointment(dto, patientName, patientId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * GET /api/appointments/user/success
     * Patient views their confirmed (paid) appointments.
     */
    @GetMapping("/user/success")
    public ResponseEntity<List<AppointmentDTO>> getSuccessfulAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long patientId = (userDetails != null) ? userDetails.getId() : null;
        return ResponseEntity.ok(appointmentService.getPatientSuccessfulAppointments(patientId));
    }

    /**
     * GET /api/appointments/user/all
     * Patient views all their appointments regardless of payment status.
     */
    @GetMapping("/user/all")
    public ResponseEntity<List<AppointmentDTO>> getAllPatientAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long patientId = (userDetails != null) ? userDetails.getId() : null;
        return ResponseEntity.ok(appointmentService.getPatientAllAppointments(patientId));
    }

    /**
     * GET /api/appointments/admin/all
     * Admin views all appointments.
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
}
