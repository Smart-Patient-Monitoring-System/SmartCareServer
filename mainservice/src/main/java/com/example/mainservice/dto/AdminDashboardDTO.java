package com.example.mainservice.dto;

public class AdminDashboardDTO {

    private final long doctorCount;
    private final long patientCount;

    public AdminDashboardDTO(long doctorCount, long patientCount) {
        this.doctorCount = doctorCount;
        this.patientCount = patientCount;
    }

    public long getDoctorCount() {
        return doctorCount;
    }

    public long getPatientCount() {
        return patientCount;
    }
}
