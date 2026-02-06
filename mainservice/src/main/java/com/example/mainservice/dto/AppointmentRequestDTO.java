package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {
    private Long doctorId;
    private Long appointmentTypeId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String reason;
}
