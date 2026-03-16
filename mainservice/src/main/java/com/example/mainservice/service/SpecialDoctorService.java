package com.example.mainservice.service;

import com.example.mainservice.dto.SpecialDoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.SpecialDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecialDoctorService {

    private final DoctorRepo doctorRepo;
    private final SpecialDoctorRepository legacyRepo;

    public SpecialDoctorService(DoctorRepo doctorRepo, SpecialDoctorRepository legacyRepo) {
        this.doctorRepo = doctorRepo;
        this.legacyRepo = legacyRepo;
    }

    // Get all doctors from the main `doctor` table
    public List<SpecialDoctorDTO> getAllDoctors() {
        return doctorRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by ID
    public SpecialDoctorDTO getDoctorById(Long id) {
        return doctorRepo.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    // Add doctor (Not used by new admin portal, but kept for compatibility)
    public SpecialDoctorDTO addDoctor(SpecialDoctorDTO dto) {
        throw new UnsupportedOperationException("Doctors should be created via the Admin Portal.");
    }

    // Update doctor (Not used by new admin portal)
    public SpecialDoctorDTO updateDoctor(Long id, SpecialDoctorDTO dto) {
         throw new UnsupportedOperationException("Doctors should be updated via the Admin Portal.");
    }

    // Delete doctor (Not used by new admin portal)
    public void deleteDoctor(Long id) {
        throw new UnsupportedOperationException("Doctors should be deleted via the Admin Portal.");
    }

    // Helper to map Doctor entity → SpecialDoctorDTO
    private SpecialDoctorDTO mapToDTO(Doctor doc) {
        return new SpecialDoctorDTO(
                doc.getId(),
                doc.getDoctorRegNo(),     // Maps to registrationNumber
                doc.getName(),
                doc.getPosition(),        // Maps to specialty
                1500.0,                   // Default consultation fee (or calculate if needed)
                null,                     // profilePhoto
                "Hospital: " + doc.getHospital(), // description
                doc.getPosition(),        // qualification
                doc.getContactNo(),       // phoneNumber
                doc.getEmail()
        );
    }
}
