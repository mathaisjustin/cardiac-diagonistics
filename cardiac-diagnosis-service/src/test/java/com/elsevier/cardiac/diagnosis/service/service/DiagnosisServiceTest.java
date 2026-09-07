package com.elsevier.cardiac.diagnosis.service.service;

import com.elsevier.cardiac.diagnosis.service.client.DiagnosisApiClient;
import com.elsevier.cardiac.diagnosis.service.dto.AdvancedSearchRequest;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResult;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisStats;
import com.elsevier.cardiac.diagnosis.service.event.BookmarkEvent;
import com.elsevier.cardiac.diagnosis.service.exception.DiagnosisNotFoundException;
import com.elsevier.cardiac.diagnosis.service.kafka.BookmarkProducer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private DiagnosisApiClient diagnosisApiClient;

    @Mock
    private BookmarkProducer bookmarkProducer;

    private DiagnosisService diagnosisService;

    @BeforeEach
    void setUp() {
        diagnosisService = new DiagnosisService(diagnosisApiClient, bookmarkProducer);
    }

    private Diagnosis diagnosis(String id, String gender, int age, int bp, String painType, String treatment) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setGender(gender);
        diagnosis.setAge(age);
        diagnosis.setBp(bp);
        diagnosis.setPainType(painType);
        diagnosis.setTreatment(treatment);
        return diagnosis;
    }

    @Test
    void getAllDiagnoses_returnsRecordsFromClient() {
        Diagnosis[] records = {
                diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication"),
                diagnosis("2", "Female", 60, 140, "Asymptomatic", "Medication")
        };
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        List<Diagnosis> result = diagnosisService.getAllDiagnoses();

        assertThat(result).hasSize(2).extracting(Diagnosis::getId).containsExactly("1", "2");
    }

    @Test
    void getAllDiagnoses_nullFromClient_returnsEmptyList() {
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(null);

        List<Diagnosis> result = diagnosisService.getAllDiagnoses();

        assertThat(result).isEmpty();
    }

    @Test
    void getDiagnosisById_found_returnsRecord() {
        Diagnosis[] records = {diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication")};
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        Diagnosis result = diagnosisService.getDiagnosisById("1");

        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    void getDiagnosisById_notFound_throws() {
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(new Diagnosis[0]);

        assertThatThrownBy(() -> diagnosisService.getDiagnosisById("missing"))
                .isInstanceOf(DiagnosisNotFoundException.class);
    }

    @Test
    void bookmarkDiagnosis_publishesEventWithSnapshottedFields() {
        Diagnosis[] records = {diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication")};
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        diagnosisService.bookmarkDiagnosis("1", "user-1");

        ArgumentCaptor<BookmarkEvent> captor = ArgumentCaptor.forClass(BookmarkEvent.class);
        verify(bookmarkProducer).publish(captor.capture());

        BookmarkEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo("user-1");
        assertThat(event.getDiagnosisId()).isEqualTo("1");
        assertThat(event.getPayload().getGender()).isEqualTo("Male");
        assertThat(event.getPayload().getTreatment()).isEqualTo("Medication");
    }

    @Test
    void bookmarkDiagnosis_recordMissing_throwsAndNeverPublishes() {
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(new Diagnosis[0]);

        assertThatThrownBy(() -> diagnosisService.bookmarkDiagnosis("missing", "user-1"))
                .isInstanceOf(DiagnosisNotFoundException.class);

        verifyNoInteractions(bookmarkProducer);
    }

    @Test
    void advancedSearch_filtersByGenderAndAgeRange() {
        Diagnosis[] records = {
                diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication"),
                diagnosis("2", "Female", 60, 140, "Asymptomatic", "Medication"),
                diagnosis("3", "Male", 70, 150, "Typical Angina", "Surgery")
        };
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        AdvancedSearchRequest request = new AdvancedSearchRequest();
        request.setGender("male");
        request.setAgeMax(50);

        List<Diagnosis> result = diagnosisService.advancedSearch(request);

        assertThat(result).extracting(Diagnosis::getId).containsExactly("1");
    }

    @Test
    void advancedSearch_noMatches_returnsEmptyList() {
        Diagnosis[] records = {
                diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication")
        };
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        AdvancedSearchRequest request = new AdvancedSearchRequest();
        request.setGender("Female");

        List<Diagnosis> result = diagnosisService.advancedSearch(request);

        assertThat(result).isEmpty();
    }

    @Test
    void analyzeByCharacteristic_unsupportedCharacteristic_throws() {
        Diagnosis[] records = {diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication")};
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        assertThatThrownBy(() -> diagnosisService.analyzeByCharacteristic("bloodType"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void analyzeByCharacteristic_groupsByGenderAndComputesDominantTreatment() {
        Diagnosis[] records = {
                diagnosis("1", "Male", 45, 130, "Typical Angina", "Medication"),
                diagnosis("2", "Male", 50, 135, "Typical Angina", "Medication"),
                diagnosis("3", "Female", 60, 140, "Asymptomatic", "Surgery")
        };
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        AnalysisResult result = diagnosisService.analyzeByCharacteristic("gender");

        assertThat(result.getTotalRecords()).isEqualTo(3);
        assertThat(result.getBreakdown()).hasSize(2);
        assertThat(result.getBreakdown().get(0).getValue()).isEqualTo("Male");
        assertThat(result.getBreakdown().get(0).getCount()).isEqualTo(2);
        assertThat(result.getBreakdown().get(0).getDominantTreatment()).isEqualTo("Medication");
    }

    @Test
    void getPublicStats_computesTotalsAndSurgeryShare() {
        Diagnosis[] records = {
                diagnosis("1", "Male", 40, 130, "Typical Angina", "Coronary Artery Bypass Graft (CABG)"),
                diagnosis("2", "Female", 60, 140, "Asymptomatic", "Medication")
        };
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(records);

        DiagnosisStats stats = diagnosisService.getPublicStats();

        assertThat(stats.getTotalRecords()).isEqualTo(2);
        assertThat(stats.getMeanAge()).isEqualTo(50.0);
        assertThat(stats.getSurgeryShare()).isEqualTo(50.0);
        assertThat(stats.getSample()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void getPublicStats_emptyDataset_returnsZeroedStats() {
        when(diagnosisApiClient.getAllDiagnoses()).thenReturn(new Diagnosis[0]);

        DiagnosisStats stats = diagnosisService.getPublicStats();

        assertThat(stats.getTotalRecords()).isZero();
        assertThat(stats.getMeanAge()).isZero();
        assertThat(stats.getSurgeryShare()).isZero();
        assertThat(stats.getSample()).isEmpty();
    }
}
