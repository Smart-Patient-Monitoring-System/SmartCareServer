package com.example.mainservice.repository;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // All appointments with a specific payment status (admin / general use)
    List<Appointment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Doctor's paid appointments (for doctor portal)
    List<Appointment> findByDoctorIdAndPaymentStatus(Long doctorId, PaymentStatus paymentStatus);

    // All of a doctor's appointments regardless of payment (full schedule)
    List<Appointment> findByDoctorId(Long doctorId);

    // Patient's paid appointments (patient portal - "My Bookings")
    List<Appointment> findByPatientIdAndPaymentStatus(Long patientId, PaymentStatus paymentStatus);

    // All of a patient's appointments regardless of payment status
    List<Appointment> findByPatientId(Long patientId);
}
