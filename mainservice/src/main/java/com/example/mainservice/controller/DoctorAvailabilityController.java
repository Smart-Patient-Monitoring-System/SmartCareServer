package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorAvailabilityDTO;
import com.example.mainservice.dto.SaveAvailabilityRequest;
import com.example.mainservice.entity.DoctorAvailability;
import com.example.mainservice.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService service;

    /**
     * GET /api/availability/doctor/{doctorId}?date=YYYY-MM-DD
     * Public — patients browse available slots for a doctor on a date.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam("date") String dateStr
    ) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<DoctorAvailability> slots = service.getAvailableSlots(doctorId, date);
            List<DoctorAvailabilityDTO> dtos = slots.stream()
                    .map(slot -> new DoctorAvailabilityDTO(
                            slot.getId(),
                            slot.getAvailableDate(),
                            slot.getAvailableTime()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD"));
        }
    }

    /**
     * GET /api/availability/doctor/{doctorId}/all
     * All slots for a doctor (including booked) — used by doctor portal to see their schedule.
     */
    @GetMapping("/doctor/{doctorId}/all")
    public ResponseEntity<List<DoctorAvailabilityDTO>> getAllSlots(@PathVariable Long doctorId) {
        List<DoctorAvailabilityDTO> dtos = service.getAllSlots(doctorId).stream()
                .map(slot -> new DoctorAvailabilityDTO(
                        slot.getId(),
                        slot.getAvailableDate(),
                        slot.getAvailableTime()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/availability
     * Doctor or admin adds availability slots.
     * Body: { doctorId, date, times: ["09:00", "10:00", ...] }
     */
    @PostMapping
    public ResponseEntity<?> addAvailability(@RequestBody SaveAvailabilityRequest request) {
        try {
            service.addAvailability(request);
            return ResponseEntity.ok(Map.of("message", "Availability slots added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/availability/{slotId}
     * Update a specific slot's date/time.
     */
    @PutMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(
            @PathVariable Long slotId,
            @RequestBody DoctorAvailabilityDTO dto
    ) {
        try {
            service.updateSlot(slotId, dto);
            return ResponseEntity.ok(Map.of("message", "Slot updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/availability/{slotId}
     * Delete a specific slot.
     */
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long slotId) {
        try {
            service.deleteSlot(slotId);
            return ResponseEntity.ok(Map.of("message", "Slot deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
