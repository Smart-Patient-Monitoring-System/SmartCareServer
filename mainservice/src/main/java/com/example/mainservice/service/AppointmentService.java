package com.example.mainservice.service;

import com.example.mainservice.dto.AppointmentDTO;
import com.example.mainservice.dto.AppointmentRequestDTO;
import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.AppointmentType;
import com.example.mainservice.entity.DoctorAvailability;
import com.example.mainservice.entity.SpecialDoctor;
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

    // ================================================================
    // BOOK APPOINTMENT (patient flow)
    // ================================================================

    @Transactional
    public AppointmentDTO bookAppointment(AppointmentRequestDTO request, String patientName, Long patientId) {
        SpecialDoctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getDoctorId()));

        AppointmentType appointmentType = appointmentTypeRepository.findById(request.getAppointmentTypeId())
                .orElseThrow(() -> new RuntimeException("Appointment type not found"));

        DoctorAvailability availableSlot = availabilityService
                .getAvailableSlots(request.getDoctorId(), request.getBookingDate())
                .stream()
                .filter(slot -> slot.getAvailableTime().equals(request.getBookingTime()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Selected time slot is not available"));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .appointmentType(appointmentType)
                .availability(availableSlot)
                .bookingDate(availableSlot.getAvailableDate())
                .bookingTime(availableSlot.getAvailableTime())
                .reason(request.getReason())
                .patientName(patientName)
                .patientId(patientId)
                .paymentStatus(PaymentStatus.PENDING)
                .appointmentStatus(AppointmentStatus.PENDING)
                .build();

        return convertToDTO(appointmentRepository.save(appointment));
    }

    // ================================================================
    // PATIENT VIEWS
    // ================================================================

    /** Patient's confirmed (paid) appointments only */
    public List<AppointmentDTO> getPatientSuccessfulAppointments(Long patientId) {
        if (patientId == null) return List.of();
        return appointmentRepository.findByPatientIdAndPaymentStatus(patientId, PaymentStatus.SUCCESS)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /** All of a patient's appointments (any payment status) */
    public List<AppointmentDTO> getPatientAllAppointments(Long patientId) {
        if (patientId == null) return List.of();
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // DOCTOR VIEWS
    // ================================================================

    /** Doctor's confirmed (paid) appointments */
    public List<AppointmentDTO> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndPaymentStatus(doctorId, PaymentStatus.SUCCESS)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /** All of a doctor's appointments regardless of payment (full schedule) */
    public List<AppointmentDTO> getAllDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN VIEWS
    // ================================================================

    /** All appointments (admin) */
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /** Admin: only paid appointments */
    public List<AppointmentDTO> getSuccessfulAppointments() {
        return appointmentRepository.findByPaymentStatus(PaymentStatus.SUCCESS)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // UTILITY
    // ================================================================

    @Transactional
    public AppointmentDTO setMeetingLink(Long appointmentId, String link) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setOnlineLink(link);
        return convertToDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentDTO confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        return convertToDTO(appointmentRepository.save(appointment));
    }

    public AppointmentDTO convertToDTO(Appointment appointment) {
        String locationOrLink = "Physical".equalsIgnoreCase(appointment.getAppointmentType().getTypeName())
                ? appointment.getPhysicalLocation()
                : appointment.getOnlineLink();

        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentId(appointment.getId());
        dto.setAvailabilityId(appointment.getAvailability() != null ? appointment.getAvailability().getId() : null);
        dto.setDoctorName(appointment.getDoctor() != null ? appointment.getDoctor().getName() : "N/A");
        dto.setSpecialty(appointment.getDoctor() != null ? appointment.getDoctor().getSpecialty() : "N/A");
        dto.setConsultationFee(appointment.getDoctor() != null ? appointment.getDoctor().getConsultationFee() : 0.0);
        dto.setDoctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        dto.setAppointmentType(appointment.getAppointmentType().getTypeName());
        dto.setLocationOrLink(locationOrLink);
        dto.setBookingDate(appointment.getBookingDate());
        dto.setBookingTime(appointment.getBookingTime());
        dto.setReason(appointment.getReason());
        dto.setPaymentStatus(appointment.getPaymentStatus().name());
        dto.setAppointmentStatus(appointment.getAppointmentStatus().name());
        dto.setPatientName(appointment.getPatientName());
        dto.setPatientId(appointment.getPatientId());
        return dto;
    }
}
