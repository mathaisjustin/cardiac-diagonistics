package com.elsevier.cardiac.diagnosis.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Public landing-page preview - intentionally exposes nothing beyond gender/age/pain_type.
public class DiagnosisSample {

    private String gender;
    private int age;

    @JsonProperty("pain_type")
    private String painType;

    public DiagnosisSample(Diagnosis diagnosis) {
        this.gender = diagnosis.getGender();
        this.age = diagnosis.getAge();
        this.painType = diagnosis.getPainType();
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public String getPainType() {
        return painType;
    }
}
