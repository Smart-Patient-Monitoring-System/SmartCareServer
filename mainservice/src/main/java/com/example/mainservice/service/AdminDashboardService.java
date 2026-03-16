package com.example.mainservice.service;

import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepo patientRepo;

    public long getDoctorCount() {
        return doctorRepo.count();
    }

    public long getPatientCount() {
        return patientRepo.count();
    }
}
