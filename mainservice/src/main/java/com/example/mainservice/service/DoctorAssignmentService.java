package com.example.mainservice.service;

import com.example.mainservice.dto.DoctorAssignmentItemDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAssignmentService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    private List<Doctor> getAssignableDoctors() {
        List<Doctor> generalDoctors = doctorRepo.findByPositionContainingIgnoreCase("General");
        if (generalDoctors.isEmpty()) {
            generalDoctors = doctorRepo.findAll();
            log.warn("No General Doctors found, using all available doctors for assignment");
        }
        return generalDoctors.stream()
                .sorted(Comparator.comparing(Doctor::getId))
                .toList();
    }

    public Long assignDoctor() {
        List<Doctor> generalDoctors = getAssignableDoctors();
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


    @Transactional
    public void assignDoctorToPatient(Long doctorId, Long patientId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setAssignedDoctorId(doctor.getId());
        patientRepo.save(patient);

        log.info("Admin assigned Doctor {} (ID: {}) to Patient {} (ID: {})",
                doctor.getName(), doctor.getId(), patient.getName(), patient.getId());
    }

    @Transactional
    public void unassignDoctorFromPatient(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patient.setAssignedDoctorId(null);
        patientRepo.save(patient);

        log.info("Admin unassigned doctor from Patient {} (ID: {})", patient.getName(), patient.getId());
    }

    public List<DoctorAssignmentItemDTO> getAllAssignments() {
        return patientRepo.findAll().stream()
                .filter(patient -> patient.getAssignedDoctorId() != null)
                .map(patient -> {
                    Doctor doctor = doctorRepo.findById(patient.getAssignedDoctorId()).orElse(null);
                    return DoctorAssignmentItemDTO.builder()
                            .patientId(patient.getId())
                            .patientName(patient.getName())
                            .patientEmail(patient.getEmail())
                            .doctorId(patient.getAssignedDoctorId())
                            .doctorName(doctor != null ? doctor.getName() : "Unknown")
                            .doctorEmail(doctor != null ? doctor.getEmail() : null)
                            .build();
                })
                .toList();
    }

    public long getPatientCountForDoctor(Long doctorId) {
        return patientRepo.countByAssignedDoctorId(doctorId);
    }

    @Transactional
    public void rebalancePatientsRoundRobin() {
        List<Doctor> doctors = getAssignableDoctors();
        if (doctors.isEmpty()) {
            log.warn("Skipping patient rebalance because no doctors are available");
            return;
        }

        List<Patient> patients = patientRepo.findAll().stream()
                .sorted(Comparator
                        .comparing(Patient::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Patient::getId))
                .toList();

        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            Doctor assignedDoctor = doctors.get(i % doctors.size());
            patient.setAssignedDoctorId(assignedDoctor.getId());
        }

        patientRepo.saveAll(patients);
        log.info("Rebalanced {} patients across {} doctors using round-robin assignment", patients.size(), doctors.size());
    }
}
