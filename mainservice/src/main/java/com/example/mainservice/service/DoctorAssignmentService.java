package com.example.mainservice.service;

import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Service for automatic doctor assignment using Round Robin algorithm.
 * Assigns patients to General Doctors based on least patient count.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAssignmentService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    /**
     * Assigns a doctor to a new patient using Round Robin algorithm.
     * Selects the General Doctor with the fewest assigned patients.
     *
     * @return The ID of the assigned doctor, or null if no doctors available
     */
    public Long assignDoctor() {
        // Get all General Doctors
        List<Doctor> generalDoctors = doctorRepo.findByPositionContainingIgnoreCase("General");

        if (generalDoctors.isEmpty()) {
            // Fallback: try to get any available doctor
            generalDoctors = doctorRepo.findAll();
            log.warn("No General Doctors found, using all available doctors for assignment");
        }

        if (generalDoctors.isEmpty()) {
            log.error("No doctors available for patient assignment");
            return null;
        }

        // Find the doctor with the fewest assigned patients (Round Robin)
        Doctor assignedDoctor = generalDoctors.stream()
                .min(Comparator.comparingLong(doctor -> patientRepo.countByAssignedDoctorId(doctor.getId())))
                .orElse(generalDoctors.get(0));

        log.info("Assigned patient to Doctor: {} (ID: {})", assignedDoctor.getName(), assignedDoctor.getId());
        return assignedDoctor.getId();
    }

    /**
     * Get the count of patients assigned to a specific doctor.
     *
     * @param doctorId The doctor's ID
     * @return Number of patients assigned to this doctor
     */
    public long getPatientCountForDoctor(Long doctorId) {
        return patientRepo.countByAssignedDoctorId(doctorId);
    }
}
