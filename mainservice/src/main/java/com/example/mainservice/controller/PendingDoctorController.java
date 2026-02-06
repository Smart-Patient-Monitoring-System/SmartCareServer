package com.example.mainservice.controller;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PendingDoctorDTO;
import com.example.mainservice.entity.PendingDoctor;
import com.example.mainservice.service.PendingDoctorService;
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
@RequestMapping("/api/pendingdoctor")

public class PendingDoctorController {
    @Autowired
    private PendingDoctorService pendingdoctorservice;

    @PostMapping("/create")
    public ResponseEntity<?> createPendingDoctor(@Valid @RequestBody PendingDoctorDTO pendingdoctorDto){
        try {
            PendingDoctor pendingdoctor = pendingdoctorservice.create(pendingdoctorDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(pendingdoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public List<PendingDoctorDTO> getAllDocters(){

        return  pendingdoctorservice.getDetails();
    }

    @DeleteMapping("/delete/{Id}")
    public String deletePendingDoctorByID(@PathVariable Long Id) {
        try {
            pendingdoctorservice.deletePendingDoctor(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<String> acceptDoctor(@PathVariable Long id) {
        pendingdoctorservice .acceptDoctor(id);
        return ResponseEntity.ok("Doctor accepted successfully");
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
