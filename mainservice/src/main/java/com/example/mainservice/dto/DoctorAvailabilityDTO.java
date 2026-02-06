
package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityDTO {
    private Long id;               // Slot ID
    private LocalDate availableDate;
    private LocalTime availableTime;
}
