package com.example.mainservice.service;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.entity.*;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.AppointmentTypeRepository;
import com.example.mainservice.repository.SpecialDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityService availabilityService;
    private final SpecialDoctorRepository doctorRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;

    // ============================
    // BOOK APPOINTMENT
    // ============================
    @Transactional
    public AppointmentDTO bookAppointment(AppointmentRequestDTO request) {

        SpecialDoctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        AppointmentType appointmentType = appointmentTypeRepository
                .findById(request.getAppointmentTypeId())
                .orElseThrow(() -> new RuntimeException("Appointment type not found"));

        DoctorAvailability availableSlot = availabilityService
                .getAvailableSlots(request.getDoctorId(), request.getBookingDate())
                .stream()
                .filter(slot -> slot.getAvailableTime().equals(request.getBookingTime()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Selected time slot not available"));

        DoctorAvailability bookedSlot =
                availabilityService.markSlotBooked(availableSlot.getId());

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .appointmentType(appointmentType)
                .availability(bookedSlot)
                .bookingDate(bookedSlot.getAvailableDate())
                .bookingTime(bookedSlot.getAvailableTime())
                .reason(request.getReason())
                .paymentStatus(PaymentStatus.PENDING)      // Payment not done yet
                .appointmentStatus(AppointmentStatus.PENDING)
                .build();

        return convertToDTO(
                appointmentRepository.save(appointment)
        );
    }

    // ============================
    // USER → ONLY SUCCESS PAYMENTS
    // ============================
    public List<AppointmentDTO> getSuccessfulAppointments() {
        return appointmentRepository
                .findByPaymentStatus(PaymentStatus.SUCCESS)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // ADMIN → ALL APPOINTMENTS
    // ============================
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // ENTITY → DTO
    // ============================
    private AppointmentDTO convertToDTO(Appointment appointment) {

        String locationOrLink =
                appointment.getAppointmentType().getTypeName().equalsIgnoreCase("Physical")
                        ? appointment.getPhysicalLocation()
                        : appointment.getOnlineLink();

        return new AppointmentDTO(
                appointment.getId(),
                appointment.getAvailability() != null
                        ? appointment.getAvailability().getId()
                        : null,
                appointment.getDoctor().getName(),
                appointment.getDoctor().getSpecialty(),
                appointment.getDoctor().getConsultationFee(),
                appointment.getAppointmentType().getTypeName(),
                locationOrLink,
                appointment.getBookingDate(),
                appointment.getBookingTime(),
                appointment.getReason(),
                appointment.getPaymentStatus().name(),
                appointment.getAppointmentStatus().name()
        );
    }
}
