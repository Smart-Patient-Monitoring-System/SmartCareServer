package com.example.mainservice.entity;

import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= RELATIONSHIPS ================= */

    @ManyToOne
    @JoinColumn(name = "availability_id")
    private DoctorAvailability availability;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private SpecialDoctor doctor;

    @ManyToOne
    @JoinColumn(name = "appointment_type_id")
    private AppointmentType appointmentType;

    /* ================= BASIC DETAILS ================= */

    private LocalDate bookingDate;

    private LocalTime bookingTime;

    private String reason;

    private String physicalLocation;

    private String onlineLink;

    /* ================= PAYMENT ================= */

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    // PayHere order_id (STRING — very important)
    @Column(unique = true)
    private String orderId;
}
