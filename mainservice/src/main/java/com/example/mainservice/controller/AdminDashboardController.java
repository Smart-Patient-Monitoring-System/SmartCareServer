package com.example.mainservice.controller;

import com.example.mainservice.dto.AdminDashboardDTO;
import com.example.mainservice.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard/counts")
    public AdminDashboardDTO getCounts() {
        return new AdminDashboardDTO(
                dashboardService.getDoctorCount(),
                dashboardService.getPatientCount()
        );
    }
}
