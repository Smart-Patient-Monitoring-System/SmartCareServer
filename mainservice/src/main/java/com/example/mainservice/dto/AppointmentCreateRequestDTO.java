
package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequestDTO {

    private Long doctorId;           // SpecialDoctor ID
    private Long appointmentTypeId;  // AppointmentType ID
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String reason;
}
