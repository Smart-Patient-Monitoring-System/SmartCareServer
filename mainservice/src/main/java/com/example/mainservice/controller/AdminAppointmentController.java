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

    // 🔹 GET ALL APPOINTMENTS
    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {

        //  ONLY SUCCESS PAYMENTS
        List<Appointment> list =
                appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS);

        System.out.println("PAID APPOINTMENTS = " + list.size());

        return list.stream().map(appointmentService::convertToDTO).toList();
    }

    // 🔹 CONFIRM APPOINTMENT
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

        return ResponseEntity.ok("Confirmed");
    }
}