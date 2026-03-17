package com.example.mainservice.service;

import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public AdminDashboardService(DoctorRepo doctorRepo, PatientRepo patientRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    public long getDoctorCount() {
        return doctorRepo.count();
    }

    public long getPatientCount() {
        return patientRepo.count();
    }
}
