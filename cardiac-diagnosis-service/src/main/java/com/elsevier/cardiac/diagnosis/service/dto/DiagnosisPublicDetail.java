package com.elsevier.cardiac.diagnosis.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiagnosisPublicDetail {

    private String id;
    private String gender;
    private int age;
    private int bp;
    private int cholesterol;
    private String diabetic;

    @JsonProperty("smoking_status")
    private String smokingStatus;

    @JsonProperty("pain_type")
    private String painType;

    public DiagnosisPublicDetail(Diagnosis diagnosis) {
        this.id = diagnosis.getId();
        this.gender = diagnosis.getGender();
        this.age = diagnosis.getAge();
        this.bp = diagnosis.getBp();
        this.cholesterol = diagnosis.getCholesterol();
        this.diabetic = diagnosis.getDiabetic();
        this.smokingStatus = diagnosis.getSmokingStatus();
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

    public int getBp() {
        return bp;
    }

    public int getCholesterol() {
        return cholesterol;
    }

    public String getDiabetic() {
        return diabetic;
    }

    public String getSmokingStatus() {
        return smokingStatus;
    }

    public String getPainType() {
        return painType;
    }
}
