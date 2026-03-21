package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.security.CustomUserDetails;
import com.example.mainservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /* ============================ BOOK APPOINTMENT ============================ */
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestBody AppointmentRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long patientId = userDetails.getId();
            AppointmentDTO appointment = appointmentService.bookAppointment(dto, patientId);
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /* ============================ GET MY SUCCESSFUL APPOINTMENTS ============================ */
    @GetMapping("/user/success")
    public ResponseEntity<List<AppointmentDTO>> getMySuccessfulAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long patientId = userDetails.getId();
        return ResponseEntity.ok(
                appointmentService.getSuccessfulAppointmentsByPatient(patientId)
        );
    }

    /* ============================ GET ALL SUCCESSFUL APPOINTMENTS (ADMIN) ============================ */
    @GetMapping("/user/success/all")
    public ResponseEntity<List<AppointmentDTO>> getSuccessfulAppointments() {
        return ResponseEntity.ok(
                appointmentService.getSuccessfulAppointments()
        );
    }

    /* ============================ GET ALL APPOINTMENTS (ADMIN) ============================ */
    @GetMapping("/admin/all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(
                appointmentService.getAllAppointments()
        );
    }

    /* ============================ RESCHEDULE APPOINTMENT ============================ */
    @PutMapping("/reschedule/{appointmentId}")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestParam Long newAvailabilityId) {
        try {
            AppointmentDTO updated = appointmentService.rescheduleAppointment(appointmentId, newAvailabilityId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
}