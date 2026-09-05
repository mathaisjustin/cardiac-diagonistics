package com.elsevier.cardiac.diagnosis.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Internal filter set built from /diagnosis/search query parameters.")
public class AdvancedSearchRequest {

    private String gender;
    private String painType;
    private Integer ageMin;
    private Integer ageMax;
    private Integer bpMin;
    private Integer bpMax;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPainType() {
        return painType;
    }

    public void setPainType(String painType) {
        this.painType = painType;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public Integer getBpMin() {
        return bpMin;
    }

    public void setBpMin(Integer bpMin) {
        this.bpMin = bpMin;
    }

    public Integer getBpMax() {
        return bpMax;
    }

    public void setBpMax(Integer bpMax) {
        this.bpMax = bpMax;
    }
}
