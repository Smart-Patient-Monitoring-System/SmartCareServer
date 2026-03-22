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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    /**
     * GET /api/admin/appointments
     * Returns all PAID appointments for admin overview panel.
     */
    @GetMapping
    public List<AppointmentDTO> getAllPaidAppointments() {
        List<Appointment> list = appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS);
        System.out.println("PAID APPOINTMENTS = " + list.size());
        return list.stream().map(appointmentService::convertToDTO).toList();
    }

    /**
     * GET /api/admin/appointments/all
     * Returns ALL appointments regardless of payment status.
     */
    @GetMapping("/all")
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    /**
     * POST /api/admin/appointments/confirm/{id}
     * Admin confirms appointment and optionally sets location or zoom link.
     * Frontend redirected here after clicking "Confirm" in admin booking panel.
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirmAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String physicalLocation,
            @RequestParam(required = false) String zoomLink
    ) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));

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
