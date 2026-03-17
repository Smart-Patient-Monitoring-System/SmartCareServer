package com.example.mainservice.service;

import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private PendingDoctorRepo pendingdoctorRepo;

    public long getDoctorCount() {
        return doctorRepo.count();
    }

    public long getPatientCount() {return patientRepo.count(); }

    public long getPendingDoctorCount() {
        return pendingdoctorRepo.count();
    }
}
