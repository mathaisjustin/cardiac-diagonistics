package com.elsevier.cardiac.diagnosis.service.event;

/**
 * Published to the {@code bookmark.created} Kafka topic when a registered user bookmarks
 * a diagnosis record. Diagnosis Service already has the record resolved at this point, so
 * the payload is a snapshot rather than an ID Bookmark Service would need to re-fetch.
 * Mirrors Bookmark Service's own BookmarkEvent exactly.
 */
public class BookmarkEvent {

    private String userId;
    private String diagnosisId;
    private DiagnosisPayload payload;

    public BookmarkEvent() {
    }

    public BookmarkEvent(String userId, String diagnosisId, DiagnosisPayload payload) {
        this.userId = userId;
        this.diagnosisId = diagnosisId;
        this.payload = payload;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public DiagnosisPayload getPayload() {
        return payload;
    }

    public void setPayload(DiagnosisPayload payload) {
        this.payload = payload;
    }
}
