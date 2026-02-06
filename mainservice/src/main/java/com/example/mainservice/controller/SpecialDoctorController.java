
package com.example.mainservice.controller;

import com.example.mainservice.dto.SpecialDoctorDTO;
import com.example.mainservice.entity.SpecialDoctor;
import com.example.mainservice.service.SpecialDoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/doctors")
public class SpecialDoctorController {

    private final SpecialDoctorService doctorService;

    public SpecialDoctorController(SpecialDoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // Get all doctors
    @GetMapping
    public List<SpecialDoctorDTO> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    // Get doctor by ID
    @GetMapping("/{id}")
    public SpecialDoctorDTO getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    // Add a new doctor
    @PostMapping
    public SpecialDoctorDTO addDoctor(@RequestBody SpecialDoctorDTO doctorDTO) {
        return doctorService.addDoctor(doctorDTO);
    }

    // Update a doctor
    @PutMapping("/{id}")
    public SpecialDoctorDTO updateDoctor(@PathVariable Long id, @RequestBody SpecialDoctorDTO doctorDTO) {
        return doctorService.updateDoctor(id, doctorDTO);
    }

    // Delete a doctor
    @DeleteMapping("/{id}")
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }
}
