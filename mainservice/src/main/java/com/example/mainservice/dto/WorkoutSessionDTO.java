package com.example.mainservice.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionDTO {
    private Long id;
    private Long patientId;
    private String name;
    private LocalDate uploadDate;
    private String source;
    private Object healthData; // Parsed JSON object
    private String fileName;
}
