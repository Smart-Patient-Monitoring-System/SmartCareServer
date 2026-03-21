package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAssignmentItemDTO {
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long doctorId;
    private String doctorName;
    private String doctorEmail;
}
