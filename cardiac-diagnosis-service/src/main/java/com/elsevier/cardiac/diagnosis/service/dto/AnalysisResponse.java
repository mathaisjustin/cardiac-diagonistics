package com.elsevier.cardiac.diagnosis.service.dto;

import java.util.Map;

public class AnalysisResponse {

    private int totalRecords;

    private Map<String, Long> treatmentCounts;

    public AnalysisResponse(
            int totalRecords,
            Map<String, Long> treatmentCounts) {

        this.totalRecords = totalRecords;
        this.treatmentCounts = treatmentCounts;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public Map<String, Long> getTreatmentCounts() {
        return treatmentCounts;
    }
}