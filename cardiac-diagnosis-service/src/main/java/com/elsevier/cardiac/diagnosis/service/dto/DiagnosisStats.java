package com.elsevier.cardiac.diagnosis.service.dto;

import java.util.List;

// Public landing-page summary - aggregate figures plus a small random sample.
public class DiagnosisStats {

    private int totalRecords;
    private double meanAge;
    private double surgeryShare;
    private List<DiagnosisSample> sample;

    public DiagnosisStats(int totalRecords, double meanAge, double surgeryShare, List<DiagnosisSample> sample) {
        this.totalRecords = totalRecords;
        this.meanAge = meanAge;
        this.surgeryShare = surgeryShare;
        this.sample = sample;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public double getMeanAge() {
        return meanAge;
    }

    public double getSurgeryShare() {
        return surgeryShare;
    }

    public List<DiagnosisSample> getSample() {
        return sample;
    }
}
