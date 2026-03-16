package com.example.mainservice.service;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.entity.*;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.AppointmentTypeRepository;
import com.example.mainservice.repository.DoctorRepo;
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
    private final DoctorRepo doctorRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;

    // ============================
    // BOOK APPOINTMENT
    // ============================
    @Transactional
    public AppointmentDTO bookAppointment(AppointmentRequestDTO request, String patientName) {

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
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
                .patientName(patientName)
                .paymentStatus(PaymentStatus.PENDING)
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
    // DOCTOR → THEIR APPOINTMENTS
    // ============================
    public List<AppointmentDTO> getDoctorAppointments(Long doctorId) {
        return appointmentRepository
                .findByDoctorIdAndPaymentStatus(doctorId, PaymentStatus.SUCCESS)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // SET ZOOM/MEETING LINK
    // ============================
    @Transactional
    public AppointmentDTO setMeetingLink(Long appointmentId, String link) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        a.setOnlineLink(link);
        return convertToDTO(appointmentRepository.save(a));
    }

    // ============================
    // CONFIRM APPOINTMENT
    // ============================
    @Transactional
    public AppointmentDTO confirmAppointment(Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        a.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        return convertToDTO(appointmentRepository.save(a));
    }

    // ============================
    // ENTITY → DTO
    // ============================
    public AppointmentDTO convertToDTO(Appointment appointment) {

        String locationOrLink =
                appointment.getAppointmentType().getTypeName().equalsIgnoreCase("Physical")
                        ? appointment.getPhysicalLocation()
                        : appointment.getOnlineLink();

        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentId(appointment.getId());
        dto.setAvailabilityId(appointment.getAvailability() != null
                ? appointment.getAvailability().getId() : null);
        dto.setDoctorName(appointment.getDoctor().getName());
        dto.setSpecialty(appointment.getDoctor().getPosition()); // mapped from position
        dto.setConsultationFee(1500.0); // default fee
        dto.setAppointmentType(appointment.getAppointmentType().getTypeName());
        dto.setLocationOrLink(locationOrLink);
        dto.setBookingDate(appointment.getBookingDate());
        dto.setBookingTime(appointment.getBookingTime());
        dto.setReason(appointment.getReason());
        dto.setPaymentStatus(appointment.getPaymentStatus().name());
        dto.setAppointmentStatus(appointment.getAppointmentStatus().name());
        dto.setPatientName(appointment.getPatientName());
        return dto;
    }
}
