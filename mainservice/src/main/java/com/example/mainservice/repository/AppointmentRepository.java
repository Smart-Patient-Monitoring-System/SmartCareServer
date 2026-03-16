package com.example.mainservice.repository;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Fetch only successful payments
    List<Appointment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Fetch appointments for a specific doctor (by SpecialDoctor ID)
    List<Appointment> findByDoctorIdAndPaymentStatus(Long doctorId, PaymentStatus paymentStatus);

    // Fetch all appointments for a specific doctor
    List<Appointment> findByDoctorId(Long doctorId);
}
