package com.elsevier.cardiac.diagnosis.service.service;

import com.elsevier.cardiac.diagnosis.service.client.DiagnosisApiClient;
import com.elsevier.cardiac.diagnosis.service.dto.AdvancedSearchRequest;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisGroup;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResult;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.exception.DiagnosisNotFoundException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    // Advanced search (registered users only) - range filters on age/bp
    public List<Diagnosis> advancedSearch(
            AdvancedSearchRequest request) {

        return getAllDiagnoses()
                .stream()

                .filter(diagnosis ->
                        request.getGender() == null
                                || diagnosis.getGender()
                                .equalsIgnoreCase(
                                        request.getGender()))

                .filter(diagnosis ->
                        request.getPainType() == null
                                || diagnosis.getPainType()
                                .equalsIgnoreCase(
                                        request.getPainType()))

                .filter(diagnosis ->
                        request.getAgeMin() == null
                                || diagnosis.getAge() >= request.getAgeMin())

                .filter(diagnosis ->
                        request.getAgeMax() == null
                                || diagnosis.getAge() <= request.getAgeMax())

                .filter(diagnosis ->
                        request.getBpMin() == null
                                || diagnosis.getBp() >= request.getBpMin())

                .filter(diagnosis ->
                        request.getBpMax() == null
                                || diagnosis.getBp() <= request.getBpMax())

                .collect(Collectors.toList());
    }

    // Treatment breakdown grouped by one characteristic, across the full
    // dataset (registered users only) - not affected by search filters.
    public AnalysisResult analyzeByCharacteristic(String characteristic) {

        List<Diagnosis> all = getAllDiagnoses();

        Map<String, List<Diagnosis>> groups =
                all.stream().collect(Collectors.groupingBy(
                        diagnosis -> groupKey(diagnosis, characteristic),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<String> orderedKeys = new ArrayList<>(groups.keySet());

        if ("age".equals(characteristic)) {
            orderedKeys.sort(Comparator.naturalOrder());
        } else {
            orderedKeys.sort((a, b) -> groups.get(b).size() - groups.get(a).size());
        }

        List<AnalysisGroup> breakdown = new ArrayList<>();

        for (String key : orderedKeys) {

            List<Diagnosis> groupRecords = groups.get(key);
            Map<String, Long> counts = treatmentCounts(groupRecords);
            Map<String, Double> percentages = treatmentPercentages(counts, groupRecords.size());
            String dominant = dominantTreatment(counts);

            breakdown.add(new AnalysisGroup(
                    key, groupRecords.size(), counts, percentages, dominant
            ));
        }

        Map<String, Long> overallCounts = treatmentCounts(all);
        Map<String, Double> overallPercentages = treatmentPercentages(overallCounts, all.size());

        return new AnalysisResult(
                characteristic, all.size(), overallCounts, overallPercentages, breakdown
        );
    }

    private String groupKey(Diagnosis diagnosis, String characteristic) {

        return switch (characteristic) {
            case "age" -> ageBucket(diagnosis.getAge());
            case "gender" -> diagnosis.getGender();
            case "painType" -> diagnosis.getPainType();
            default -> throw new IllegalArgumentException(
                    "Unsupported characteristic: " + characteristic);
        };
    }

    private String ageBucket(int age) {
        int decade = (age / 10) * 10;
        return decade + "-" + (decade + 9);
    }

    private Map<String, Long> treatmentCounts(List<Diagnosis> records) {

        return records.stream()
                .collect(Collectors.groupingBy(
                        Diagnosis::getTreatment,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    private Map<String, Double> treatmentPercentages(Map<String, Long> counts, int total) {

        Map<String, Double> percentages = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : counts.entrySet()) {

            double percentage = total == 0
                    ? 0.0
                    : Math.round(entry.getValue() * 1000.0 / total) / 10.0;

            percentages.put(entry.getKey(), percentage);
        }

        return percentages;
    }

    private String dominantTreatment(Map<String, Long> counts) {

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
