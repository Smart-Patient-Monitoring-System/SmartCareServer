package com.example.mainservice.controller;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;

    // 🔹 GET ALL APPOINTMENTS
    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {

        //  ONLY SUCCESS PAYMENTS
        List<Appointment> list =
                appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS);

        System.out.println("PAID APPOINTMENTS = " + list.size());

        return list.stream().map(a -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setAppointmentId(a.getId());
            dto.setAvailabilityId(a.getAvailability() != null ? a.getAvailability().getId() : null);
            dto.setDoctorName(a.getDoctor() != null ? a.getDoctor().getName() : "N/A");
            dto.setSpecialty(a.getDoctor() != null ? a.getDoctor().getSpecialty() : "N/A");
            dto.setConsultationFee(a.getDoctor() != null ? a.getDoctor().getConsultationFee() : 0.0);
            dto.setAppointmentType(a.getAppointmentType().getTypeName());
            dto.setLocationOrLink(a.getPhysicalLocation() != null ? a.getPhysicalLocation() : a.getOnlineLink());
            dto.setBookingDate(a.getBookingDate());
            dto.setBookingTime(a.getBookingTime());
            dto.setReason(a.getReason());
            dto.setPaymentStatus(a.getPaymentStatus().name());
            dto.setAppointmentStatus(a.getAppointmentStatus().name());
            dto.setPatientName(a.getPatientName());
            return dto;
        }).toList();
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
