package com.example.mainservice.controller;

import com.example.mainservice.dto.AdminDashboardDTO;
import com.example.mainservice.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController


public class AdminDashboardController {
    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/api/dashboard/counts")
    public AdminDashboardDTO getCounts() {

        long doctorCount = dashboardService.getDoctorCount();
        long patientCount = dashboardService.getPatientCount();
        long pendingdoctorCount = dashboardService.getPendingDoctorCount();

        return new AdminDashboardDTO(doctorCount, patientCount, pendingdoctorCount);
    }


}
