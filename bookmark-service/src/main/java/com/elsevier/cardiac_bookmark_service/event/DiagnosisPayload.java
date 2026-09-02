package com.elsevier.cardiac_bookmark_service.event;

public class DiagnosisPayload {

    private String gender;
    private Integer age;
    private String bp;
    private String painType;
    private String treatment;


    public DiagnosisPayload() {
    }


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


    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }


    public String getPainType() {
        return painType;
    }

    public void setPainType(String painType) {
        this.painType = painType;
    }


    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
}
