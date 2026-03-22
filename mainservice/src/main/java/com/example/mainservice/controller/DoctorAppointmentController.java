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

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;
    private final SpecialDoctorRepository specialDoctorRepository;
    private final AppointmentRepository appointmentRepository;

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
