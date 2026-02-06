
package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAvailabilityRequest {
    private Long doctorId;        // Doctor for whom availability is added
    private LocalDate date;       // The date selected
    private List<LocalTime> times; // Multiple time slots
}
