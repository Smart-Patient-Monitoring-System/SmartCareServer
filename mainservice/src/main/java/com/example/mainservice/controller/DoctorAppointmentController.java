package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.entity.SpecialDoctor;
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

    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getDoctorAppointmentsByEmail(@RequestParam String email) {
        SpecialDoctor doctor = specialDoctorRepository.findByEmail(email).orElse(null);
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctor.getId()));
    }

    @PutMapping("/{appointmentId}/link")
    public ResponseEntity<?> setMeetingLink(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body
    ) {
        try {
            return ResponseEntity.ok(appointmentService.setMeetingLink(appointmentId, body.get("link")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long appointmentId) {
        try {
            return ResponseEntity.ok(appointmentService.confirmAppointment(appointmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
