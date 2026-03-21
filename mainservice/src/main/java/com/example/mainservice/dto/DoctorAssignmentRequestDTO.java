package com.example.mainservice.dto;

import lombok.Data;

@Data
public class DoctorAssignmentRequestDTO {
    private Long doctorId;
    private Long patientId;
}
