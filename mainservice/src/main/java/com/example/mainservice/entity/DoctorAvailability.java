package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate availableDate;

    private LocalTime availableTime;

    // FK to Doctor entity (used by Doctor.availabilities)
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    // FK to SpecialDoctor entity (used by SpecialDoctor.availabilities)
    @ManyToOne
    @JoinColumn(name = "special_doctor_id")
    private SpecialDoctor specialDoctor;

    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;  // ensures default false
}
