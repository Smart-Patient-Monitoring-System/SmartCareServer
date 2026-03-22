package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequestDTO dto, Principal principal) {
        try {
            String patientName = principal != null ? principal.getName() : "Unknown";
            return ResponseEntity.ok(appointmentService.bookAppointment(dto, patientName));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user/success")
    public ResponseEntity<List<AppointmentDTO>> getSuccessfulAppointments() {
        System.out.println("=== /api/appointments/user/success HIT by Patient ===");
        try {
            List<AppointmentDTO> list = appointmentService.getSuccessfulAppointments();
            System.out.println("=== Returning " + list.size() + " appointments ===");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("=== ERROR in getSuccessfulAppointments: " + e.getMessage() + " ===");
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
}
