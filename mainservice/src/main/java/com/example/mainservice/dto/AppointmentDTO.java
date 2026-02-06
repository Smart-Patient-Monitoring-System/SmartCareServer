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
    private String appointmentType;   // Physical / Online
    private String locationOrLink;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String reason;
    private String paymentStatus;
    private String appointmentStatus;
}
