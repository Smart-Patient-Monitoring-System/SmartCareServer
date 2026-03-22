package com.example.mainservice.repository;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** All appointments with a given payment status */
    List<Appointment> findByPaymentStatus(PaymentStatus paymentStatus);

    /** Doctor's paid appointments (SpecialDoctor FK) */
    List<Appointment> findByDoctorIdAndPaymentStatus(Long doctorId, PaymentStatus paymentStatus);

    /** All of a doctor's appointments regardless of payment */
    List<Appointment> findByDoctorId(Long doctorId);
}
