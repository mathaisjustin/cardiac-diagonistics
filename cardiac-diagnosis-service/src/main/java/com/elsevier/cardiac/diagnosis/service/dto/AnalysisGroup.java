package com.elsevier.cardiac.diagnosis.service.dto;

import java.util.Map;

public class AnalysisGroup {

    private String value;
    private int count;
    private Map<String, Long> treatmentCounts;
    private Map<String, Double> treatmentPercentages;
    private String dominantTreatment;

    public AnalysisGroup(
            String value,
            int count,
            Map<String, Long> treatmentCounts,
            Map<String, Double> treatmentPercentages,
            String dominantTreatment) {

        this.value = value;
        this.count = count;
        this.treatmentCounts = treatmentCounts;
        this.treatmentPercentages = treatmentPercentages;
        this.dominantTreatment = dominantTreatment;
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    public Map<String, Long> getTreatmentCounts() {
        return treatmentCounts;
    }

    public Map<String, Double> getTreatmentPercentages() {
        return treatmentPercentages;
    }

    public String getDominantTreatment() {
        return dominantTreatment;
    }
}
