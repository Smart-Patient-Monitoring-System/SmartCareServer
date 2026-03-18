package com.example.vitalReports.domain.model;

import com.example.vitalReports.domain.enums.TriageLevel;

public class VitalAssessment {

    private VitalStatus vitalStatus;
    private TriageLevel triageLevel;

    public VitalAssessment(VitalStatus vitalStatus, TriageLevel triageLevel) {
        this.vitalStatus = vitalStatus;
        this.triageLevel = triageLevel;
    }

    public VitalStatus getVitalStatus() {
        return vitalStatus;
    }

    public TriageLevel getTriageLevel() {
        return triageLevel;
    }
}
