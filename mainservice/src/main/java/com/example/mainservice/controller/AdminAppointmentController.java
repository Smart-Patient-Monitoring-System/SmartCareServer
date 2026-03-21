package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;

    /* ================= GET ALL PAID APPOINTMENTS ================= */
    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {

        List<Appointment> appointments =
                appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS);

        return appointments.stream().map(a -> new AppointmentDTO(
                a.getId(),
                a.getAvailability() != null ? a.getAvailability().getId() : null,
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                a.getDoctor() != null ? a.getDoctor().getName() : "N/A",
                a.getDoctor() != null ? a.getDoctor().getSpecialty() : "N/A",
                a.getDoctor() != null ? a.getDoctor().getConsultationFee() : 0.0,
                a.getAppointmentType() != null ? a.getAppointmentType().getTypeName() : "N/A",
                a.getPhysicalLocation() != null ? a.getPhysicalLocation() : a.getOnlineLink(),
                a.getBookingDate(),
                a.getBookingTime(),
                a.getReason(),
                a.getPaymentStatus().name(),
                a.getAppointmentStatus().name()
        )).toList();
    }

    /* ================= CONFIRM APPOINTMENT ================= */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirmAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String physicalLocation,
            @RequestParam(required = false) String zoomLink
    ) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getAppointmentType() != null &&
                "Physical".equalsIgnoreCase(appointment.getAppointmentType().getTypeName())) {

            appointment.setPhysicalLocation(physicalLocation);

        } else {

            appointment.setOnlineLink(zoomLink);
        }

        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        return ResponseEntity.ok("Appointment confirmed successfully");
    }

    /* ================= UPDATE APPOINTMENT DATE & TIME ================= */
    @PutMapping("/update-date/{id}")
    public ResponseEntity<String> updateAppointmentDate(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String bookingDate = body.get("bookingDate");
        String bookingTime = body.get("bookingTime");

        try {

            if (bookingDate != null && !bookingDate.isBlank()) {
                LocalDate newDate = LocalDate.parse(bookingDate);
                appointment.setBookingDate(newDate);
            }

            if (bookingTime != null && !bookingTime.isBlank()) {
                // Convert String to LocalTime to match entity
                LocalTime newTime = LocalTime.parse(bookingTime);
                appointment.setBookingTime(newTime);
            }

            appointmentRepository.save(appointment);

            return ResponseEntity.ok("Appointment date/time updated successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date or time format");
        }
    }

}