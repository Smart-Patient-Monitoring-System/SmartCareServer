package com.example.mainservice.controller;

import com.example.mainservice.entity.AppointmentType;
import com.example.mainservice.repository.AppointmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeRepository typeRepository;

    /**
     * GET /api/appointment-types
     * Returns all appointment types. Auto-seeds Physical + Online if DB is empty.
     */
    @GetMapping
    public List<AppointmentType> getAllTypes() {
        List<AppointmentType> types = typeRepository.findAll();
        if (types.isEmpty()) {
            AppointmentType physical = new AppointmentType();
            physical.setTypeName("Physical");

            AppointmentType online = new AppointmentType();
            online.setTypeName("Online");

            typeRepository.saveAll(List.of(physical, online));
            types = typeRepository.findAll();
        }
        return types;
    }

    /**
     * POST /api/appointment-types
     * Admin adds a new appointment type.
     */
    @PostMapping
    public ResponseEntity<?> addType(@RequestBody AppointmentType type) {
        try {
            return ResponseEntity.ok(typeRepository.save(type));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/appointment-types/{id}
     * Admin removes an appointment type.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteType(@PathVariable Long id) {
        try {
            typeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Type deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
