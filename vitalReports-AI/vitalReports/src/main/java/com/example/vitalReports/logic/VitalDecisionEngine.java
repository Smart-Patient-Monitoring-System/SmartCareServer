package com.example.vitalReports.logic;

import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.domain.model.VitalStatus;

public interface VitalDecisionEngine {
    VitalStatus evaluate(VitalReading reading);
}
