package com.example.mainservice.service;

import com.example.mainservice.dto.MedicalEventCreateDTO;
import com.example.mainservice.dto.MedicalEventResponseDTO;
import com.example.mainservice.dto.MedicalSummaryDTO;

import java.util.List;

public interface MedicalEventService {

    void addEvent(Long patientId, MedicalEventCreateDTO dto, String createdBy);

    List<MedicalEventResponseDTO> getEvents(Long patientId, String from, String to);

    MedicalSummaryDTO getSummary(Long patientId);
}
