
package com.example.mainservice.service;

import com.example.mainservice.dto.SpecialDoctorDTO;
import com.example.mainservice.entity.SpecialDoctor;
import com.example.mainservice.repository.SpecialDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecialDoctorService {

    private final SpecialDoctorRepository repository;

    public SpecialDoctorService(SpecialDoctorRepository repository) {
        this.repository = repository;
    }

    // Get all doctors fro databse Get all doctors from database  Convert each entity to DTO Return list
    public List<SpecialDoctorDTO> getAllDoctors() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by ID
    public SpecialDoctorDTO getDoctorById(Long id) {
        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    // Add doctor
    public SpecialDoctorDTO addDoctor(SpecialDoctorDTO dto) {
        SpecialDoctor doctor = new SpecialDoctor();
        doctor.setRegistrationNumber(dto.getRegistrationNumber());
        doctor.setName(dto.getName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setProfilePhoto(dto.getProfilePhoto());
        doctor.setDescription(dto.getDescription());
        doctor.setQualification(dto.getQualification());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setEmail(dto.getEmail());

        SpecialDoctor saved = repository.save(doctor);
        return mapToDTO(saved);
    }

    // Update doctor
    public SpecialDoctorDTO updateDoctor(Long id, SpecialDoctorDTO dto) {
        SpecialDoctor doctor = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setRegistrationNumber(dto.getRegistrationNumber());
        doctor.setName(dto.getName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setProfilePhoto(dto.getProfilePhoto());
        doctor.setDescription(dto.getDescription());
        doctor.setQualification(dto.getQualification());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setEmail(dto.getEmail());

        SpecialDoctor updated = repository.save(doctor);
        return mapToDTO(updated);
    }

    // Delete doctor
    public void deleteDoctor(Long id) {
        repository.deleteById(id);
    }

    // Helper to map entity → DTO
    private SpecialDoctorDTO mapToDTO(SpecialDoctor doc) {
        return new SpecialDoctorDTO(
                doc.getId(),
                doc.getRegistrationNumber(),
                doc.getName(),
                doc.getSpecialty(),
                doc.getConsultationFee(),
                doc.getProfilePhoto(),
                doc.getDescription(),
                doc.getQualification(),
                doc.getPhoneNumber(),
                doc.getEmail()
        );
    }
}
