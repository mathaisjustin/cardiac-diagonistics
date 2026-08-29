package com.elsevier.cardiac.diagnosis.service.service;

import com.elsevier.cardiac.diagnosis.service.client.DiagnosisApiClient;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResponse;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisSearchRequest;
import exception.DiagnosisNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiagnosisService {

    private final DiagnosisApiClient diagnosisApiClient;

    public DiagnosisService(DiagnosisApiClient diagnosisApiClient) {
        this.diagnosisApiClient = diagnosisApiClient;
    }

    // Get all diagnosis records
    public List<Diagnosis> getAllDiagnoses() {

        Diagnosis[] diagnoses =
                diagnosisApiClient.getAllDiagnoses();

        if (diagnoses == null) {
            return List.of();
        }

        return Arrays.asList(diagnoses);
    }

    // Get diagnosis by ID
    public Diagnosis getDiagnosisById(String id) {

        return getAllDiagnoses()
                .stream()
                .filter(diagnosis ->
                        diagnosis.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new DiagnosisNotFoundException(id));
    }

    // Advanced search
    public List<Diagnosis> search(
            DiagnosisSearchRequest request) {

        return getAllDiagnoses()
                .stream()

                .filter(diagnosis ->
                        request.getGender() == null
                                || request.getGender().isBlank()
                                || diagnosis.getGender()
                                .equalsIgnoreCase(
                                        request.getGender()))

                .filter(diagnosis ->
                        request.getAge() == null
                                || diagnosis.getAge()
                                == request.getAge())

                .filter(diagnosis ->
                        request.getBp() == null
                                || diagnosis.getBp()
                                == request.getBp())

                .filter(diagnosis ->
                        request.getPainType() == null
                                || request.getPainType().isBlank()
                                || diagnosis.getPainType()
                                .equalsIgnoreCase(
                                        request.getPainType()))

                .collect(Collectors.toList());
    }

    // Analyze treatment recommendations
    // based on patient characteristics
    public AnalysisResponse analyzeTreatment(
            DiagnosisSearchRequest request) {

        List<Diagnosis> matchingDiagnoses =
                getAllDiagnoses()
                        .stream()

                        .filter(diagnosis ->
                                request.getGender() == null
                                        || request.getGender().isBlank()
                                        || diagnosis.getGender()
                                        .equalsIgnoreCase(
                                                request.getGender()))

                        .filter(diagnosis ->
                                request.getAge() == null
                                        || diagnosis.getAge()
                                        == request.getAge())

                        .filter(diagnosis ->
                                request.getBp() == null
                                        || diagnosis.getBp()
                                        == request.getBp())

                        .filter(diagnosis ->
                                request.getPainType() == null
                                        || request.getPainType().isBlank()
                                        || diagnosis.getPainType()
                                        .equalsIgnoreCase(
                                                request.getPainType()))

                        .toList();

        Map<String, Long> treatmentCounts =
                matchingDiagnoses.stream()
                        .collect(Collectors.groupingBy(
                                Diagnosis::getTreatment,
                                LinkedHashMap::new,
                                Collectors.counting()
                        ));

        return new AnalysisResponse(
                matchingDiagnoses.size(),
                treatmentCounts
        );
    }

    // Check whether diagnosis exists
    public boolean diagnosisExists(String id) {

        return getAllDiagnoses()
                .stream()
                .anyMatch(diagnosis ->
                        diagnosis.getId().equals(id));
    }

    // Create diagnosis
    public Diagnosis createDiagnosis(
            Diagnosis diagnosis) {

        return diagnosisApiClient.createDiagnosis(
                diagnosis);
    }
}