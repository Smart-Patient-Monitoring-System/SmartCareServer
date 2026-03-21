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

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;
    private final SpecialDoctorRepository specialDoctorRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * GET /api/doctor/appointments/{doctorId}
     * Get all paid appointments for a specific doctor by their ID.
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    /**
     * GET /api/doctor/appointments/by-email?email=...
     * Get appointments for the currently logged-in special doctor (by email).
     * Used by the doctor portal when doctor logs in via their email.
     */
    @GetMapping("/by-email")
    public ResponseEntity<?> getDoctorAppointmentsByEmail(
            @RequestParam(required = false) String email,
            Principal principal
    ) {
        // If no email param, try to get from JWT principal
        String lookupEmail = (email != null && !email.isBlank()) ? email
                : (principal != null ? principal.getName() : null);

        if (lookupEmail == null) {
            return ResponseEntity.badRequest().body("Email parameter required");
        }

        SpecialDoctor doctor = specialDoctorRepository.findByEmail(lookupEmail).orElse(null);
        if (doctor == null) {
            return ResponseEntity.ok(List.of()); // Return empty list, not error
        }
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctor.getId()));
    }

    /**
     * GET /api/doctor/appointments/all/{doctorId}
     * Get ALL appointments (paid + unpaid) for a doctor — for doctor's full schedule view.
     */
    @GetMapping("/all/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAllDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAllDoctorAppointments(doctorId));
    }

    /**
     * POST /api/doctor/appointments/confirm/{id}
     * Doctor confirms an appointment and sets meeting link or physical location.
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirmAppointment(
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

        return ResponseEntity.ok("Appointment confirmed");
    }

    /**
     * POST /api/doctor/appointments/cancel/{id}
     * Doctor cancels an appointment.
     */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long id) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        a.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(a);
        return ResponseEntity.ok("Appointment cancelled");
    }
}
