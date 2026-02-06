package com.example.mainservice.repository;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Fetch only successful payments
    List<Appointment> findByPaymentStatus(PaymentStatus paymentStatus);
}
