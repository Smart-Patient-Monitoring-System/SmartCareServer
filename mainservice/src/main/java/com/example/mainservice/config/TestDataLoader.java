package com.example.mainservice.config;

import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.VitalSigns;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.repository.VitalSignsRepository;
import com.example.mainservice.service.DoctorAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Data loader for:
 * 1. Assigning doctors to existing patients (runs in ALL profiles)
 * 2. Loading test patients and vital signs (runs only in 'dev' profile)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestDataLoader {

    private final PatientRepo patientRepo;
    private final VitalSignsRepository vitalSignsRepository;
    private final DoctorAssignmentService doctorAssignmentService;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    /**
     * RUNS IN ALL MODES - Assigns doctors to any existing patients without one.
     * This ensures round-robin works even for existing database records.
     */
    
    @Bean
    public CommandLineRunner assignDoctorsToPatients() {
        return args -> {
            assignExistingPatientsWithoutDoctor();
        };
    }

    /**
     * RUNS ONLY IN DEV MODE - Loads test patients for development/testing.
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner loadTestData() {
        return args -> {
            // Check if test patients already exist
            if (patientRepo.count() >= 10) {
                log.info("Test data already loaded. Skipping new test patients...");
                return;
            }

            log.info("Loading test patient data...");

            String[][] patientData = {
                    { "John Smith", "john.smith@email.com", "1990-05-15", "M", "A+", "123 Main St, Colombo",
                            "0771234567" },
                    { "Sarah Johnson", "sarah.j@email.com", "1985-08-22", "F", "O+", "456 Lake Rd, Kandy",
                            "0772345678" },
                    { "Michael Chen", "m.chen@email.com", "1978-12-10", "M", "B+", "789 Hill St, Galle", "0773456789" },
                    { "Emily Davis", "emily.d@email.com", "1992-03-18", "F", "AB+", "321 Park Ave, Negombo",
                            "0774567890" },
                    { "David Wilson", "d.wilson@email.com", "1988-07-25", "M", "O-", "654 Ocean Dr, Matara",
                            "0775678901" },
                    { "Lisa Anderson", "lisa.a@email.com", "1995-11-30", "F", "A-", "987 Forest Ln, Nuwara Eliya",
                            "0776789012" },
                    { "Robert Taylor", "r.taylor@email.com", "1982-09-08", "M", "B-", "147 River Rd, Kurunegala",
                            "0777890123" },
                    { "Jennifer Martinez", "j.martinez@email.com", "1990-01-12", "F", "AB-",
                            "258 Mountain View, Badulla", "0778901234" },
                    { "William Brown", "w.brown@email.com", "1987-06-20", "M", "O+", "369 Valley St, Anuradhapura",
                            "0779012345" },
                    { "Jessica Garcia", "j.garcia@email.com", "1993-04-05", "F", "A+", "741 Beach Rd, Trincomalee",
                            "0770123456" }
            };

            String[] rooms = { "ICU-101", "ICU-102", "Ward-201", "Ward-202", "Ward-203",
                    "Private-301", "Private-302", "Emergency-401", "CCU-501", "ICU-103" };

            String[] conditions = {
                    "Diabetes Type 2",
                    "Hypertension",
                    "Asthma",
                    "None",
                    "Cardiac Arrhythmia",
                    "COPD",
                    "None",
                    "Post-Surgery Recovery",
                    "Pneumonia",
                    "Chronic Kidney Disease"
            };

            for (int i = 0; i < patientData.length; i++) {
                String[] data = patientData[i];

                // Create patient
                Patient patient = Patient.builder()
                        .name(data[0])
                        .email(data[1])
                        .dateOfBirth(LocalDate.parse(data[2]))
                        .gender(data[3])
                        .bloodType(data[4])
                        .address(data[5])
                        .contactNo(data[6])
                        .nicNo(generateNIC(i))
                        .username(data[1].split("@")[0])
                        .password(passwordEncoder.encode("password123"))
                        .guardiansName("Guardian " + (i + 1))
                        .guardiansContactNo("077" + String.format("%07d", 9000000 + i))
                        .city("Colombo")
                        .district("Western")
                        .postalCode("00100")
                        .medicalConditions(conditions[i])
                        .allergies(i % 3 == 0 ? "Penicillin" : "None")
                        .build();

                // Auto-assign doctor using Round Robin
                Long doctorId = doctorAssignmentService.assignDoctor();
                patient.setAssignedDoctorId(doctorId);

                Patient savedPatient = patientRepo.save(patient);
                log.info("Created patient: {} (ID: {}) - Assigned to Doctor ID: {}",
                        savedPatient.getName(), savedPatient.getId(), doctorId);

                // Create vital signs for each patient
                createVitalsForPatient(savedPatient.getId(), rooms[i]);
            }

            log.info("Successfully loaded {} test patients", patientData.length);
        };
    }

    /**
     * Assign existing patients who don't have an assigned doctor
     */
    private void assignExistingPatientsWithoutDoctor() {
        var unassignedPatients = patientRepo.findAll().stream()
                .filter(p -> p.getAssignedDoctorId() == null)
                .toList();

        if (unassignedPatients.isEmpty()) {
            log.info("All existing patients already have doctors assigned.");
            return;
        }

        log.info("Found {} existing patients without assigned doctors. Assigning now...", unassignedPatients.size());

        for (Patient patient : unassignedPatients) {
            Long doctorId = doctorAssignmentService.assignDoctor();
            patient.setAssignedDoctorId(doctorId);
            patientRepo.save(patient);
            log.info("Assigned existing patient: {} (ID: {}) to Doctor ID: {}",
                    patient.getName(), patient.getId(), doctorId);
        }

        log.info("Finished assigning {} existing patients to doctors.", unassignedPatients.size());
    }

    private void createVitalsForPatient(Long patientId, String room) {
        // Create 3 vital sign records for each patient (simulating history)
        for (int i = 0; i < 3; i++) {
            VitalSigns vitals = new VitalSigns();
            vitals.setPatientId(patientId);
            vitals.setRoom(room);

            // Random but realistic vital signs
            vitals.setHeartRate(generateHeartRate());
            vitals.setTemperature(generateTemperature());
            vitals.setSpo2(generateSpO2());
            vitals.setBloodPressureSystolic(generateBPSystolic());
            vitals.setBloodPressureDiastolic(generateBPDiastolic());
            vitals.setBloodSugar(90.0 + random.nextDouble() * 50); // 90-140 mg/dL

            vitals.setMeasurementDateTime(LocalDateTime.now().minusHours(i * 2));
            vitals.setNotes(i == 0 ? "Latest reading" : "Historical reading " + (i + 1));

            vitalSignsRepository.save(vitals);
        }
    }

    private int generateHeartRate() {
        // 60-100 normal, 40-60 low, 100-150 high
        int[] ranges = {
                55 + random.nextInt(15), // 55-70 (normal-low)
                70 + random.nextInt(20), // 70-90 (normal)
                90 + random.nextInt(30) // 90-120 (normal-high)
        };
        return ranges[random.nextInt(ranges.length)];
    }

    private double generateTemperature() {
        // 97-99°F normal, occasionally 99-101°F
        if (random.nextDouble() < 0.8) {
            return 97.0 + random.nextDouble() * 2.0; // 97-99°F
        } else {
            return 99.0 + random.nextDouble() * 2.0; // 99-101°F
        }
    }

    private int generateSpO2() {
        // 95-100% normal, 90-95% concerning, below 90 critical
        if (random.nextDouble() < 0.7) {
            return 96 + random.nextInt(5); // 96-100%
        } else if (random.nextDouble() < 0.9) {
            return 92 + random.nextInt(4); // 92-96%
        } else {
            return 88 + random.nextInt(4); // 88-92%
        }
    }

    private int generateBPSystolic() {
        // 90-120 normal, 120-140 elevated, 140+ high
        if (random.nextDouble() < 0.6) {
            return 100 + random.nextInt(20); // 100-120
        } else if (random.nextDouble() < 0.8) {
            return 120 + random.nextInt(20); // 120-140
        } else {
            return 140 + random.nextInt(20); // 140-160
        }
    }

    private int generateBPDiastolic() {
        // 60-80 normal, 80-90 elevated
        if (random.nextDouble() < 0.7) {
            return 65 + random.nextInt(15); // 65-80
        } else {
            return 80 + random.nextInt(10); // 80-90
        }
    }

    private String generateNIC(int index) {
        // Generate dummy NIC number
        return String.format("199%d%08d", (index % 10), 12345670 + index);
    }
}
