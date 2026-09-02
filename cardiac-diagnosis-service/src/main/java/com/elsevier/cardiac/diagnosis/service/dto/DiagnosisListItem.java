package com.elsevier.cardiac.diagnosis.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiagnosisListItem {

    private String id;
    private String gender;
    private int age;

    @JsonProperty("pain_type")
    private String painType;

    public DiagnosisListItem(Diagnosis diagnosis) {
        this.id = diagnosis.getId();
        this.gender = diagnosis.getGender();
        this.age = diagnosis.getAge();
        this.painType = diagnosis.getPainType();
    }

    public String getId() {
        return id;
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
