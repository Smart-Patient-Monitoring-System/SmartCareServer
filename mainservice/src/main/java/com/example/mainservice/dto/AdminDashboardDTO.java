package com.example.mainservice.dto;

public class AdminDashboardDTO {
    private long doctorCount;
    private long patientCount;
    private long pendingdoctorCount;

    public AdminDashboardDTO(long doctorCount, long patientCount, long pendingdoctorCount) {
        this.doctorCount = doctorCount;

        this.patientCount = patientCount;

        this.pendingdoctorCount = pendingdoctorCount;

    }

    public long getDoctorCount() {
        return doctorCount;
    }

    public long getPatientCount() {
        return patientCount;
    }

    public long getPendingDoctorCount() {
        return pendingdoctorCount;
    }


}
