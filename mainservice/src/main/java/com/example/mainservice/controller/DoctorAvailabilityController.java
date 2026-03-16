package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorAvailabilityDTO;
import com.example.mainservice.dto.SaveAvailabilityRequest;
import com.example.mainservice.entity.DoctorAvailability;
import com.example.mainservice.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor

public class DoctorAvailabilityController {

    private final DoctorAvailabilityService service;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // ENDPOINT TO MIGRATE EXISTING AVAILABILITY TO NEW DOCTOR TABLE
    @GetMapping("/fix-db")
    public String fixDb() {
        try {
            // 1. Temporarily disable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

            // 2. Map existing doctor_availability records from special_doctor IDs to the new doctor IDs
            String updateAvailabilitySql = "UPDATE doctor_availability da " +
                               "JOIN special_doctor sd ON da.doctor_id = sd.id " +
                               "JOIN doctor d ON sd.registration_number = d.doctor_reg_no " +
                               "SET da.doctor_id = d.id";
            int updatedAvail = jdbcTemplate.update(updateAvailabilitySql);

            // 3. Map existing appointments records from special_doctor IDs to the new doctor IDs
            String updateAppointmentsSql = "UPDATE appointments a " +
                               "JOIN special_doctor sd ON a.doctor_id = sd.id " +
                               "JOIN doctor d ON sd.registration_number = d.doctor_reg_no " +
                               "SET a.doctor_id = d.id";
            int updatedAppt = jdbcTemplate.update(updateAppointmentsSql);

            // 4. Drop the old foreign key constraints
            try {
                // Find and drop the foreign key constraint on doctor_availability
                String findAvailabilityFkSql = "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                                   "WHERE TABLE_SCHEMA = 'healthcare' AND TABLE_NAME = 'doctor_availability' " +
                                   "AND REFERENCED_TABLE_NAME = 'special_doctor'";
                
                List<String> availFkNames = jdbcTemplate.queryForList(findAvailabilityFkSql, String.class);
                for (String fkName : availFkNames) {
                    jdbcTemplate.execute("ALTER TABLE doctor_availability DROP FOREIGN KEY " + fkName);
                }

                // Find and drop the foreign key constraint on appointments
                String findApptFkSql = "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                                   "WHERE TABLE_SCHEMA = 'healthcare' AND TABLE_NAME = 'appointments' " +
                                   "AND REFERENCED_TABLE_NAME = 'special_doctor'";
                
                List<String> apptFkNames = jdbcTemplate.queryForList(findApptFkSql, String.class);
                for (String fkName : apptFkNames) {
                    jdbcTemplate.execute("ALTER TABLE appointments DROP FOREIGN KEY " + fkName);
                }
            } catch (Exception e) {
                System.out.println("Could not drop FK: " + e.getMessage());
            }

            // 5. Re-enable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

            return "Successfully migrated " + updatedAvail + " availability slots and " + updatedAppt + " appointments to the new Doctor table! Please restart your Spring Boot backend to finalize.";
        } catch (Exception e) {
            return "Error during migration: " + e.getMessage();
        }
    }

    // ✅ Get available slots for a doctor on a specific date
    @GetMapping("/doctor/{doctorId}")
    public List<DoctorAvailabilityDTO> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam("date") String dateStr
    ) {
        LocalDate date = LocalDate.parse(dateStr);

        // Convert entities to DTOs for API response
        List<DoctorAvailability> slots = service.getAvailableSlots(doctorId, date);
        return slots.stream()
                .map(slot -> new DoctorAvailabilityDTO(
                        slot.getId(),
                        slot.getAvailableDate(),
                        slot.getAvailableTime()
                ))
                .collect(Collectors.toList());
    }

    // Add availability
    @PostMapping
    public void addAvailability(@RequestBody SaveAvailabilityRequest request) {
        service.addAvailability(request);
    }

    // Update slot
    @PutMapping("/{slotId}")
    public void updateSlot(@PathVariable Long slotId, @RequestBody DoctorAvailabilityDTO dto) {
        service.updateSlot(slotId, dto);
    }

    // Delete slot
    @DeleteMapping("/{slotId}")
    public void deleteSlot(@PathVariable Long slotId) {
        service.deleteSlot(slotId);
    }
}
