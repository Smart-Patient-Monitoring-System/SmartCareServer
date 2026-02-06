
package com.example.mainservice.controller;

import com.example.mainservice.entity.AppointmentType;
import com.example.mainservice.repository.AppointmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeRepository typeRepository;

    // GET all appointment types
    @GetMapping
    public List<AppointmentType> getAllTypes() {
        List<AppointmentType> types = typeRepository.findAll();

        if (types.isEmpty()) {
            AppointmentType p = new AppointmentType();
            p.setTypeName("Physical");

            AppointmentType o = new AppointmentType();
            o.setTypeName("Online");

            typeRepository.saveAll(List.of(p, o));
            types = typeRepository.findAll();
        }

        return types;
    }


    @PostMapping
    public AppointmentType addType(@RequestBody AppointmentType type) {
        return typeRepository.save(type);
    }

}
