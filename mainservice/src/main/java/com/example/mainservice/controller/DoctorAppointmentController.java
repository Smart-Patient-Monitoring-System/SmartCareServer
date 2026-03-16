package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.repository.DoctorRepo;
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
    private final DoctorRepo doctorRepository;

    // ============================
    // GET APPOINTMENTS FOR DOCTOR (by SpecialDoctor ID)
    // ============================
    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    // ============================
    // GET APPOINTMENTS BY DOCTOR EMAIL (for Doctor Portal login match)
    // ============================
    @GetMapping("/by-email")
    public ResponseEntity<?> getDoctorAppointmentsByEmail(@RequestParam String email) {
        System.out.println("=== Fetching appointments for Doctor email: " + email);
        com.example.mainservice.entity.Doctor doctor = doctorRepository.findByEmail(email).orElse(null);
        if (doctor == null) {
            System.out.println("Doctor not found for email: " + email);
            return ResponseEntity.ok(List.of()); // No matching Doctor = no appointments
        }
        System.out.println("Found Doctor ID: " + doctor.getId() + ". Fetching appointments...");
        List<AppointmentDTO> appts = appointmentService.getDoctorAppointments(doctor.getId());
        System.out.println("Found " + appts.size() + " appointments.");
        return ResponseEntity.ok(appts);
    }

    // ============================
    // SET ZOOM / MEETING LINK
    // ============================
    @PutMapping("/{appointmentId}/link")
    public ResponseEntity<?> setMeetingLink(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String link = body.get("link");
            return ResponseEntity.ok(appointmentService.setMeetingLink(appointmentId, link));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================
    // CONFIRM APPOINTMENT
    // ============================
    @PutMapping("/{appointmentId}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long appointmentId) {
        try {
            return ResponseEntity.ok(appointmentService.confirmAppointment(appointmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
