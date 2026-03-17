package com.example.mainservice.service;

import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAssignmentService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public Long assignDoctor() {
        List<Doctor> generalDoctors = doctorRepo.findByPositionContainingIgnoreCase("General");
        if (generalDoctors.isEmpty()) {
            generalDoctors = doctorRepo.findAll();
            log.warn("No General Doctors found, using all available doctors for assignment");
        }
        if (generalDoctors.isEmpty()) {
            log.error("No doctors available for patient assignment");
            return null;
        }

        Doctor assignedDoctor = generalDoctors.stream()
                .min(Comparator.comparingLong(doctor -> patientRepo.countByAssignedDoctorId(doctor.getId())))
                .orElse(generalDoctors.get(0));

        log.info("Assigned patient to Doctor: {} (ID: {})", assignedDoctor.getName(), assignedDoctor.getId());
        return assignedDoctor.getId();
    }

    public long getPatientCountForDoctor(Long doctorId) {
        return patientRepo.countByAssignedDoctorId(doctorId);
    }
}
