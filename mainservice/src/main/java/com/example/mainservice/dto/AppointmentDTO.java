package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {

    private Long appointmentId;
    private Long availabilityId;
    private String doctorName;
    private String specialty;
    private Double consultationFee;
    private String appointmentType;    // "Physical" or "Online"
    private String locationOrLink;     // physical address OR zoom link (set after confirmation)
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String reason;
    private String paymentStatus;      // PENDING / SUCCESS / FAILED
    private String appointmentStatus;  // PENDING / CONFIRMED / CANCELLED / COMPLETED
    private String patientName;
    private Long patientId;            // patient DB ID for per-patient filtering
    private Long doctorId;             // doctor ID for convenience on frontend
}
