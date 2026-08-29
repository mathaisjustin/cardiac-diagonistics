package com.elsevier.cardiac.diagnosis.service.dto;

public class DiagnosisSearchRequest {

    private String gender;

    private Integer age;

    private Integer bp;

    private String painType;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getBp() {
        return bp;
    }

    public void setBp(Integer bp) {
        this.bp = bp;
    }

    public String getPainType() {
        return painType;
    }

    public void setPainType(String painType) {
        this.painType = painType;
    }
}