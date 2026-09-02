package com.elsevier.cardiac.diagnosis.service.event;

/**
 * Snapshot fields carried inside a BookmarkEvent - mirrors Bookmark Service's own
 * DiagnosisPayload exactly (field names and JSON shape must match what its Kafka
 * consumer expects).
 */
public class DiagnosisPayload {

    private String gender;
    private Integer age;
    private String bp;
    private String painType;
    private String treatment;

    public DiagnosisPayload() {
    }

    public DiagnosisPayload(String gender, Integer age, String bp, String painType, String treatment) {
        this.gender = gender;
        this.age = age;
        this.bp = bp;
        this.painType = painType;
        this.treatment = treatment;
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
