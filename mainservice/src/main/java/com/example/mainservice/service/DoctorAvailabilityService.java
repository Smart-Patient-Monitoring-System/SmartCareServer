package com.example.mainservice.service;

import com.example.mainservice.dto.DoctorAvailabilityDTO;
import com.example.mainservice.dto.SaveAvailabilityRequest;
import com.example.mainservice.entity.DoctorAvailability;
import com.example.mainservice.entity.SpecialDoctor;
import com.example.mainservice.repository.DoctorAvailabilityRepository;
import com.example.mainservice.repository.SpecialDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository repository;
    private final SpecialDoctorRepository doctorRepository;

    /** Get available (not booked) slots for a doctor on a specific date */
    public List<DoctorAvailability> getAvailableSlots(Long doctorId, LocalDate date) {
        return repository.findBySpecialDoctorIdAndAvailableDateAndIsBookedFalse(doctorId, date);
    }

    /** Get ALL slots for a doctor (including booked) — for schedule view */
    public List<DoctorAvailability> getAllSlots(Long doctorId) {
        return repository.findBySpecialDoctorId(doctorId);
    }

    /** Add multiple time slots for a doctor on a date (skips duplicates) */
    public void addAvailability(SaveAvailabilityRequest request) {
        SpecialDoctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getDoctorId()));

        List<DoctorAvailability> slots = request.getTimes().stream()
                .filter(time -> !repository.existsBySpecialDoctorIdAndAvailableDateAndAvailableTime(
                        request.getDoctorId(), request.getDate(), time))
                .map(time -> DoctorAvailability.builder()
                        .specialDoctor(doctor)
                        .availableDate(request.getDate())
                        .availableTime(time)
                        .isBooked(false)
                        .build())
                .collect(Collectors.toList());

        repository.saveAll(slots);
    }

    /** Update a slot's date and/or time */
    public void updateSlot(Long slotId, DoctorAvailabilityDTO dto) {
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
        slot.setAvailableDate(dto.getAvailableDate());
        slot.setAvailableTime(dto.getAvailableTime());
        repository.save(slot);
    }

    /** Delete a slot */
    public void deleteSlot(Long slotId) {
        repository.deleteById(slotId);
    }

    /** Mark slot as booked — called during appointment booking */
    @Transactional
    public DoctorAvailability markSlotBooked(Long slotId) {
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
        if (slot.getIsBooked()) {
            throw new RuntimeException("Slot already booked");
        }
        slot.setIsBooked(true);
        return repository.save(slot);
    }

    /** Mark slot as available again — called on payment failure */
    @Transactional
    public DoctorAvailability markSlotAvailable(Long slotId) {
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
        slot.setIsBooked(false);
        return repository.save(slot);
    }
}
