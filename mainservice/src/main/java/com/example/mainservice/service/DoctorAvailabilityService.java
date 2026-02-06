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

    // ✅ GET available slots (return entities, not DTOs)
    public List<DoctorAvailability> getAvailableSlots(Long doctorId, LocalDate date) {
        return repository.findByDoctorIdAndAvailableDateAndIsBookedFalse(doctorId, date);
    }

    // ADD multiple slots
    public void addAvailability(SaveAvailabilityRequest request) {
        SpecialDoctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorAvailability> slots = request.getTimes().stream()
                .map(time -> DoctorAvailability.builder()
                        .doctor(doctor)
                        .availableDate(request.getDate())
                        .availableTime(time)
                        .isBooked(false)
                        .build())
                .collect(Collectors.toList());

        repository.saveAll(slots);
    }

    // UPDATE single slot
    public void updateSlot(Long slotId, DoctorAvailabilityDTO dto) {
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setAvailableDate(dto.getAvailableDate());
        slot.setAvailableTime(dto.getAvailableTime());

        repository.save(slot);
    }

    // DELETE single slot
    public void deleteSlot(Long slotId) {
        repository.deleteById(slotId);
    }

    // MARK SLOT AS BOOKED (transactional to prevent double booking)
    @Transactional
    public DoctorAvailability markSlotBooked(Long slotId) {
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getIsBooked()) {
            throw new RuntimeException("Slot already booked");
        }

        slot.setIsBooked(true);
        return repository.save(slot);
    }
}
