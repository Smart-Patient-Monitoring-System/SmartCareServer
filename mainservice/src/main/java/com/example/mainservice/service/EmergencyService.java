package com.example.mainservice.service;

import com.example.mainservice.entity.EmergencyAlert;
import com.example.mainservice.entity.EmergencyContact;
import com.example.mainservice.entity.Hospital;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.EmergencyAlertRepository;
import com.example.mainservice.repository.EmergencyContactRepository;
import com.example.mainservice.repository.HospitalRepository;
import com.example.mainservice.repository.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyContactRepository contactRepository;
    private final HospitalRepository hospitalRepository;
    private final EmergencyAlertRepository alertRepository;
    private final PatientRepo patientRepository;
    private final SmsService smsService;
    private final LocationService locationService;

    // Emergency Contact Methods
    public List<EmergencyContact> getUserContacts(Long userId) {
        return contactRepository.findByUserId(userId);
    }

    public EmergencyContact addContact(EmergencyContact contact) {
        return contactRepository.save(contact);
    }

    public EmergencyContact updateContact(Long userId, Long contactId, EmergencyContact updatedContact) {
        EmergencyContact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        contact.setContactName(updatedContact.getContactName());
        contact.setPhoneNumber(updatedContact.getPhoneNumber());
        contact.setRelationship(updatedContact.getRelationship());
        contact.setIsPrimary(updatedContact.getIsPrimary());

        return contactRepository.save(contact);
    }

    public void deleteContact(Long userId, Long contactId) {
        EmergencyContact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        contactRepository.delete(contact);
    }

    // Hospital Methods
    public List<Hospital> getNearestHospitals(Double latitude, Double longitude, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5;
        }
        return hospitalRepository.findNearestHospitals(latitude, longitude, limit);
    }

    public Hospital getNearestHospital(Double latitude, Double longitude) {
        List<Hospital> hospitals = hospitalRepository.findNearestHospitals(latitude, longitude, 1);
        return hospitals.isEmpty() ? null : hospitals.get(0);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Hospital addHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    // Emergency Alert Methods
    @Transactional
    public EmergencyAlert createEmergencyAlert(Long userId, EmergencyAlert alert) {
        // Get patient details
        Patient patient = patientRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        alert.setUserId(userId);
        alert.setStatus("ACTIVE");
        alert.setCreatedAt(LocalDateTime.now());

        // Use patient's stored location if not provided
        if (alert.getLatitude() == null || alert.getLongitude() == null) {
            alert.setLatitude(patient.getLatitude());
            alert.setLongitude(patient.getLongitude());
        }

        // Find nearest hospital
        if (alert.getLatitude() != null && alert.getLongitude() != null) {
            Hospital nearestHospital = getNearestHospital(alert.getLatitude(), alert.getLongitude());
            if (nearestHospital != null) {
                alert.setNearestHospitalId(nearestHospital.getId());

                // Calculate ETA
                Integer eta = calculateETA(alert.getLatitude(), alert.getLongitude(),
                        nearestHospital.getLatitude(), nearestHospital.getLongitude());
                alert.setAmbulanceEta(eta);
            }
        }

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // Send SMS to 1990 Suwa Sariya if ambulance is called
        if (alert.getAmbulanceCalled()) {
            String address = patient.getAddress() != null ? patient.getAddress() :
                    locationService.getAddressFromCoordinates(alert.getLatitude(), alert.getLongitude());

            smsService.sendEmergencyAlert(
                    patient.getName(),
                    patient.getContactNo(),
                    address,
                    alert.getLatitude(),
                    alert.getLongitude(),
                    alert.getAlertType()
            );
        }

        // Notify emergency contacts
        if (alert.getContactsNotified()) {
            notifyEmergencyContacts(userId, savedAlert, patient);
        }

        return savedAlert;
    }

    public EmergencyAlert updateAlertStatus(Long alertId, String status) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(status);
        if ("RESOLVED".equals(status) || "CANCELLED".equals(status)) {
            alert.setResolvedAt(LocalDateTime.now());
        }

        return alertRepository.save(alert);
    }

    public List<EmergencyAlert> getUserAlerts(Long userId) {
        return alertRepository.findByUserId(userId);
    }

    public EmergencyAlert getActiveAlert(Long userId) {
        return alertRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE")
                .orElse(null);
    }

    // Notification method with SMS integration
    private void notifyEmergencyContacts(Long userId, EmergencyAlert alert, Patient patient) {
        List<EmergencyContact> contacts = contactRepository.findByUserId(userId);

        String location = patient.getAddress() != null ? patient.getAddress() :
                locationService.getAddressFromCoordinates(alert.getLatitude(), alert.getLongitude());

        for (EmergencyContact contact : contacts) {
            smsService.notifyEmergencyContact(
                    contact.getPhoneNumber(),
                    patient.getName(),
                    alert.getAlertType(),
                    location
            );
        }

        System.out.println("Notified " + contacts.size() + " emergency contacts for user " + userId);
    }

    // Calculate ETA
    public Integer calculateETA(Double fromLat, Double fromLng, Double toLat, Double toLng) {
        double distance = calculateDistance(fromLat, fromLng, toLat, toLng);
        return (int) Math.ceil((distance / 40.0) * 60);
    }

    // Haversine formula for distance calculation
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


    /**
     * Auto-create emergency contact from guardian info during patient registration
     */
    @Transactional
    public void createEmergencyContactFromGuardian(Patient patient) {
        if (patient.getGuardiansName() != null && patient.getGuardiansContactNo() != null) {
            EmergencyContact contact = new EmergencyContact();
            contact.setUserId(patient.getId());
            contact.setContactName(patient.getGuardiansName());
            contact.setPhoneNumber(patient.getGuardiansContactNo());
            contact.setRelationship(patient.getGuardianRelationship() != null ?
                    patient.getGuardianRelationship() : "Guardian");
            contact.setIsPrimary(true);

            contactRepository.save(contact);
            System.out.println("Auto-created emergency contact for patient: " + patient.getName());
        }
    }
}