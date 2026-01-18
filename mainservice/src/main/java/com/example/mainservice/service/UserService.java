package com.example.mainservice.service;

import com.example.mainservice.entity.Admin;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.AdminRepo;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.security.CustomUserDetails;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final AdminRepo adminRepo;

    public UserService(@Lazy DoctorRepo doctorRepo, @Lazy PatientRepo patientRepo, @Lazy AdminRepo adminRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.adminRepo = adminRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try admin first (highest priority)
        Admin admin = adminRepo.findByUsername(username).orElse(null);
        if (admin != null) {
            return new CustomUserDetails(
                    admin.getId(),
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getEmail(),
                    admin.getName(),
                    "ADMIN"
            );
        }

        // Try doctor
        Doctor doctor = doctorRepo.findByUsername(username).orElse(null);
        if (doctor != null) {
            return new CustomUserDetails(
                    doctor.getId(),
                    doctor.getUsername(),
                    doctor.getPassword(),
                    doctor.getEmail(),
                    doctor.getName(),
                    "DOCTOR"
            );
        }

        // Then patient
        Patient patient = patientRepo.findByUsername(username).orElse(null);
        if (patient != null) {
            return new CustomUserDetails(
                    patient.getId(),
                    patient.getUsername(),
                    patient.getPassword(),
                    patient.getEmail(),
                    patient.getName(),
                    "PATIENT"
            );
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
