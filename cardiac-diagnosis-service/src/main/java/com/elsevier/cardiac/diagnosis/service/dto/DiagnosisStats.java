package com.elsevier.cardiac.diagnosis.service.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

// Public landing-page summary - aggregate figures plus a small random sample.
@Schema(description = "Aggregate summary of the full dataset plus a random sample, for "
        + "anonymous/landing-page display.")
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
