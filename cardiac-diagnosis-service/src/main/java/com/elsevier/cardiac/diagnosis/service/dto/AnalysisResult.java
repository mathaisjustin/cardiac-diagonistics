package com.elsevier.cardiac.diagnosis.service.dto;

import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private String characteristic;
    private int totalRecords;
    private Map<String, Long> overallTreatmentCounts;
    private Map<String, Double> overallTreatmentPercentages;
    private List<AnalysisGroup> breakdown;

    public AnalysisResult(
            String characteristic,
            int totalRecords,
            Map<String, Long> overallTreatmentCounts,
            Map<String, Double> overallTreatmentPercentages,
            List<AnalysisGroup> breakdown) {

        this.characteristic = characteristic;
        this.totalRecords = totalRecords;
        this.overallTreatmentCounts = overallTreatmentCounts;
        this.overallTreatmentPercentages = overallTreatmentPercentages;
        this.breakdown = breakdown;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public Map<String, Long> getOverallTreatmentCounts() {
        return overallTreatmentCounts;
    }

    public Map<String, Double> getOverallTreatmentPercentages() {
        return overallTreatmentPercentages;
    }

    public List<AnalysisGroup> getBreakdown() {
        return breakdown;
    }
}
