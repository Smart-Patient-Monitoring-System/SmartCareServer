package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    /**
     * GET /api/admin/appointments
     * Returns ALL paid appointments for admin overview.
     */
    @GetMapping
    public List<AppointmentDTO> getAllPaidAppointments() {
        return appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS)
                .stream()
                .map(appointmentService::convertToDTO)
                .toList();
    }

    /**
     * GET /api/admin/appointments/all
     * Returns ALL appointments regardless of payment status (for full admin view).
     */
    @GetMapping("/all")
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    /**
     * POST /api/admin/appointments/confirm/{id}
     * Admin confirms an appointment, setting location or zoom link.
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
            a.setPhysicalLocation(physicalLocation);
        } else {
            a.setOnlineLink(zoomLink);
        }

        a.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(a);

        return ResponseEntity.ok("Appointment confirmed successfully");
    }

    /**
     * POST /api/admin/appointments/cancel/{id}
     * Admin cancels an appointment.
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
