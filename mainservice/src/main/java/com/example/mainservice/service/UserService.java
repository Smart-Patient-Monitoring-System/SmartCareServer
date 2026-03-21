package com.example.mainservice.service;

import com.example.mainservice.entity.Admin;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.SpecialDoctor;
import com.example.mainservice.repository.AdminRepo;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.repository.SpecialDoctorRepository;
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
    private final SpecialDoctorRepository specialDoctorRepository;

    public UserService(
            @Lazy DoctorRepo doctorRepo,
            @Lazy PatientRepo patientRepo,
            @Lazy AdminRepo adminRepo,
            @Lazy SpecialDoctorRepository specialDoctorRepository
    ) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.adminRepo = adminRepo;
        this.specialDoctorRepository = specialDoctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String identifier = (username == null) ? "" : username.trim();

        // 1. Try admin first (highest priority)
        Admin admin = adminRepo.findByUsername(identifier).orElse(null);
        if (admin == null) admin = adminRepo.findByEmail(identifier).orElse(null);
        if (admin != null) {
            return new CustomUserDetails(
                    admin.getId(), admin.getUsername(), admin.getPassword(),
                    admin.getEmail(), admin.getName(), "ADMIN"
            );
        }

        // 2. Try general doctor (internal staff)
        Doctor doctor = doctorRepo.findByUsername(identifier).orElse(null);
        if (doctor == null) doctor = doctorRepo.findByEmail(identifier).orElse(null);
        if (doctor != null) {
            return new CustomUserDetails(
                    doctor.getId(), doctor.getUsername(), doctor.getPassword(),
                    doctor.getEmail(), doctor.getName(), "DOCTOR"
            );
        }

        // 3. Try special doctor (booking/consultation doctors)
        SpecialDoctor specialDoctor = specialDoctorRepository.findByEmail(identifier).orElse(null);
        if (specialDoctor != null) {
            return new CustomUserDetails(
                    specialDoctor.getId(),
                    specialDoctor.getEmail(), // username = email for SpecialDoctors
                    // SpecialDoctors don't have passwords in this system — they are managed by admin
                    // Use a placeholder; actual auth goes through admin-managed flow
                    "",
                    specialDoctor.getEmail(),
                    specialDoctor.getName(),
                    "SPECIAL_DOCTOR"
            );
        }

        // 4. Try patient
        Patient patient = patientRepo.findByUsername(identifier).orElse(null);
        if (patient == null) patient = patientRepo.findByEmail(identifier).orElse(null);
        if (patient != null) {
            return new CustomUserDetails(
                    patient.getId(), patient.getUsername(), patient.getPassword(),
                    patient.getEmail(), patient.getName(), "PATIENT"
            );
        }

        throw new UsernameNotFoundException("User not found: " + identifier);
    }
}
