package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorAvailabilityDTO;
import com.example.mainservice.dto.SaveAvailabilityRequest;
import com.example.mainservice.dto.SpecialDoctorDTO;
import com.example.mainservice.service.DoctorAvailabilityService;
import com.example.mainservice.service.SpecialDoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class SpecialDoctorController {

    private final SpecialDoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

    public SpecialDoctorController(
            SpecialDoctorService doctorService,
            DoctorAvailabilityService availabilityService
    ) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    // ── DOCTOR CRUD (admin manages) ───────────────────────────────

    @GetMapping
    public List<SpecialDoctorDTO> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(doctorService.getDoctorById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addDoctor(@RequestBody SpecialDoctorDTO doctorDTO) {
        try {
            return ResponseEntity.ok(doctorService.addDoctor(doctorDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody SpecialDoctorDTO doctorDTO) {
        try {
            return ResponseEntity.ok(doctorService.updateDoctor(id, doctorDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── AVAILABILITY (admin/doctor manages slots inline) ──────────

    /**
     * GET /api/doctors/{id}/availability?date=YYYY-MM-DD
     * Get available slots for a doctor — used during patient booking.
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) String date
    ) {
        try {
            if (date != null && !date.isBlank()) {
                java.time.LocalDate localDate = java.time.LocalDate.parse(date);
                List<DoctorAvailabilityDTO> slots = availabilityService.getAvailableSlots(id, localDate)
                        .stream()
                        .map(s -> new DoctorAvailabilityDTO(s.getId(), s.getAvailableDate(), s.getAvailableTime()))
                        .toList();
                return ResponseEntity.ok(slots);
            } else {
                // Return all slots (doctor schedule view)
                List<DoctorAvailabilityDTO> slots = availabilityService.getAllSlots(id)
                        .stream()
                        .map(s -> new DoctorAvailabilityDTO(s.getId(), s.getAvailableDate(), s.getAvailableTime()))
                        .toList();
                return ResponseEntity.ok(slots);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/doctors/{id}/availability
     * Admin adds availability slots for a doctor.
     */
    @PostMapping("/{id}/availability")
    public ResponseEntity<?> addAvailability(
            @PathVariable Long id,
            @RequestBody SaveAvailabilityRequest request
    ) {
        try {
            request.setDoctorId(id); // ensure path ID takes precedence
            availabilityService.addAvailability(request);
            return ResponseEntity.ok(Map.of("message", "Availability added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/doctors/{id}/availability/{slotId}
     * Admin removes a specific availability slot.
     */
    @DeleteMapping("/{id}/availability/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id, @PathVariable Long slotId) {
        try {
            availabilityService.deleteSlot(slotId);
            return ResponseEntity.ok(Map.of("message", "Slot removed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
