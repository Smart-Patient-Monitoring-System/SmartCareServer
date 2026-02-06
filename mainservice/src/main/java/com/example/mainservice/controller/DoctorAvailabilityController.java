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
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService service;

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
