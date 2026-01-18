package com.example.mainservice.controller;


import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller //initialize as a controller
@RestController //initialize rest API s
@RequestMapping("/api/doctor")
public class DoctorController {
    @Autowired
    private DoctorService doctorservice;

    @PostMapping("/create")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody DoctorDTO doctorDto){
        try {
            Doctor doctor = doctorservice.create(doctorDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public List<DoctorDTO> getAllDocters(){

        return  doctorservice.getDetails();
    }

    @DeleteMapping("/delete/{Id}")
    public String deleteDoctorByID(@PathVariable Long Id) {
        try {
            doctorservice.deleteDoctor(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }

    @PutMapping("/update/{Id}")
    public ResponseEntity<DoctorDTO> updateDoctorByID(
            @PathVariable Long Id,
            @RequestBody DoctorDTO doctorDto) {
        try {
            DoctorDTO updatedDoctor = doctorservice.updateDoctor(Id, doctorDto);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = errors.values().stream()
                .findFirst()
                .orElse("Validation failed");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }

}
