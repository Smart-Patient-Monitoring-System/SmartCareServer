package com.example.mainservice.service;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.EmergencyContact;
import com.example.mainservice.repository.EmergencyContactRepository;
import com.example.mainservice.repository.PatientRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepo patientrepo;
    private final PasswordEncoder passwordEncoder;
    private final EmergencyContactRepository emergencyContactRepository;
    private final LocationService locationService;

    public Patient create(PatientDTO patient){

        // Auto-calculate coordinates from address
        Double latitude = patient.getLatitude();
        Double longitude = patient.getLongitude();

        if ((latitude == null || longitude == null) && patient.getAddress() != null) {
            String fullAddress = buildFullAddress(patient);
            Double[] coordinates = locationService.getCoordinatesFromAddress(fullAddress);
            latitude = coordinates[0];
            longitude = coordinates[1];
        }

        Patient p =Patient.builder()
                .id(patient.getId())
                .name(patient.getName())
                .dateOfBirth(patient.getDateOfBirth())
                .address(patient.getAddress())
                .email(patient.getEmail())
                .nicNo(patient.getNicNo())
                .gender(patient.getGender())
                .contactNo(patient.getContactNo())
                .guardiansName(patient.getGuardiansName())
                .guardiansContactNo(patient.getGuardiansContactNo())
                .username(patient.getUsername())
                .password(passwordEncoder.encode(patient.getPassword()))
                .bloodType(patient.getBloodType())

                // NEW: Emergency fields
                .city(patient.getCity())
                .district(patient.getDistrict())
                .postalCode(patient.getPostalCode())
                .latitude(latitude)
                .longitude(longitude)
                .guardianRelationship(patient.getGuardianRelationship())
                .guardianEmail(patient.getGuardianEmail())
                .medicalConditions(patient.getMedicalConditions())
                .allergies(patient.getAllergies())
                .currentMedications(patient.getCurrentMedications())
                .emergencyNotes(patient.getEmergencyNotes())
                .build();
        Patient savedPatient = patientrepo.save(p);

        // Auto-create emergency contact from guardian info
        createEmergencyContactFromGuardian(savedPatient);

        return savedPatient;

    }

    public List<PatientDTO> getDetails(){
        return patientrepo.findAll().stream().map( p -> PatientDTO.builder()
                .Id(p.getId())
                .name(p.getName())
                .dateOfBirth(p.getDateOfBirth())
                .address(p.getAddress())
                .email(p.getEmail())
                .nicNo(p.getNicNo())
                .gender(p.getGender())
                .contactNo(p.getContactNo())
                .guardiansName(p.getGuardiansName())
                .guardiansContactNo(p.getGuardiansContactNo())
                .bloodType(p.getBloodType())
                .password(p.getPassword())
                .username(p.getUsername())
                // NEW: Emergency fields
                .city(p.getCity())
                .district(p.getDistrict())
                .postalCode(p.getPostalCode())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .guardianRelationship(p.getGuardianRelationship())
                .guardianEmail(p.getGuardianEmail())
                .medicalConditions(p.getMedicalConditions())
                .allergies(p.getAllergies())
                .currentMedications(p.getCurrentMedications())
                .emergencyNotes(p.getEmergencyNotes())
                .build()).toList();
    }

    public void deletePatient(Long Id){

        patientrepo.deleteById(Id);
    }

    @Transactional
    public PatientDTO updatePatient(Long Id, PatientDTO dto){
        Patient p = patientrepo.findById(Id).orElseThrow();

        if(dto.getName()!=null) p.setName(dto.getName());
        if(dto.getDateOfBirth()!=null) p.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getAddress() != null) {
            p.setAddress(dto.getAddress());
            // Recalculate coordinates if address changed
            String fullAddress = buildFullAddress(dto);
            Double[] coordinates = locationService.getCoordinatesFromAddress(fullAddress);
            p.setLatitude(coordinates[0]);
            p.setLongitude(coordinates[1]);
        }
        if(dto.getEmail()!=null) p.setEmail(dto.getEmail());
        if(dto.getNicNo()!=null) p.setNicNo(dto.getNicNo());
        if(dto.getGender()!=null) p.setGender(dto.getGender());
        if(dto.getContactNo()!=null) p.setContactNo(dto.getContactNo());
        if(dto.getGuardiansName()!=null) p.setGuardiansName(dto.getGuardiansName());
        if(dto.getGuardiansContactNo()!=null) p.setGuardiansContactNo(dto.getGuardiansContactNo());
        if(dto.getBloodType()!=null) p.setBloodType(dto.getBloodType());
        if(dto.getPassword()!=null) p.setPassword(dto.getPassword());
        if(dto.getUsername()!=null) p.setUsername(dto.getUsername());

        // NEW: Update emergency fields
        if (dto.getCity() != null) p.setCity(dto.getCity());
        if (dto.getDistrict() != null) p.setDistrict(dto.getDistrict());
        if (dto.getPostalCode() != null) p.setPostalCode(dto.getPostalCode());
        if (dto.getLatitude() != null) p.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) p.setLongitude(dto.getLongitude());
        if (dto.getGuardianRelationship() != null) p.setGuardianRelationship(dto.getGuardianRelationship());
        if (dto.getGuardianEmail() != null) p.setGuardianEmail(dto.getGuardianEmail());
        if (dto.getMedicalConditions() != null) p.setMedicalConditions(dto.getMedicalConditions());
        if (dto.getAllergies() != null) p.setAllergies(dto.getAllergies());
        if (dto.getCurrentMedications() != null) p.setCurrentMedications(dto.getCurrentMedications());
        if (dto.getEmergencyNotes() != null) p.setEmergencyNotes(dto.getEmergencyNotes());

        Patient updatedPatient = patientrepo.save(p);
        return convertToDTO(updatedPatient);


    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setAddress(patient.getAddress());
        dto.setEmail(patient.getEmail());
        dto.setNicNo(patient.getNicNo());
        dto.setContactNo(patient.getContactNo());
        dto.setGender(patient.getGender());
        dto.setGuardiansName(patient.getGuardiansName());
        dto.setGuardiansContactNo(patient.getGuardiansContactNo());
        dto.setBloodType(patient.getBloodType());
        dto.setPassword(patient.getPassword());
        dto.setUsername(patient.getUsername());

        // NEW: Emergency fields
        dto.setCity(patient.getCity());
        dto.setDistrict(patient.getDistrict());
        dto.setPostalCode(patient.getPostalCode());
        dto.setLatitude(patient.getLatitude());
        dto.setLongitude(patient.getLongitude());
        dto.setGuardianRelationship(patient.getGuardianRelationship());
        dto.setGuardianEmail(patient.getGuardianEmail());
        dto.setMedicalConditions(patient.getMedicalConditions());
        dto.setAllergies(patient.getAllergies());
        dto.setCurrentMedications(patient.getCurrentMedications());
        dto.setEmergencyNotes(patient.getEmergencyNotes());
        return dto;
    }
    /**
     * Auto-create emergency contact from guardian info
     */
    @Transactional
    public void createEmergencyContactFromGuardian(Patient patient) {
        if (patient.getGuardiansName() != null && patient.getGuardiansContactNo() != null) {
            // Check if emergency contact already exists
            List<EmergencyContact> existing = emergencyContactRepository.findByUserId(patient.getId());
            boolean hasGuardianContact = existing.stream()
                    .anyMatch(c -> c.getPhoneNumber().equals(patient.getGuardiansContactNo()));

            if (!hasGuardianContact) {
                EmergencyContact contact = new EmergencyContact();
                contact.setUserId(patient.getId());
                contact.setContactName(patient.getGuardiansName());
                contact.setPhoneNumber(patient.getGuardiansContactNo());
                contact.setRelationship(patient.getGuardianRelationship() != null ?
                        patient.getGuardianRelationship() : "Guardian");
                contact.setIsPrimary(true);

                emergencyContactRepository.save(contact);
                System.out.println("Auto-created emergency contact for patient: " + patient.getName());
            }
        }
    }

    /**
     * Build full address string
     */
    private String buildFullAddress(PatientDTO patient) {
        StringBuilder address = new StringBuilder();
        if (patient.getAddress() != null) address.append(patient.getAddress());
        if (patient.getCity() != null) address.append(", ").append(patient.getCity());
        if (patient.getDistrict() != null) address.append(", ").append(patient.getDistrict());
        address.append(", Sri Lanka"); // Default country
        return address.toString();
    }

    /**
     * Get patient by username (for emergency panel)
     */
    public Patient getPatientByUsername(String username) {
        return patientrepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }
}