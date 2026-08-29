package com.elsevier.cardiac.diagnosis.service.client;

import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import exception.ExternalApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class DiagnosisApiClient {

    private final RestTemplate restTemplate;
    private final String diagnosisApiUrl;

    public DiagnosisApiClient(
            @Value("${external.diagnosis-api-url}")
            String diagnosisApiUrl) {

        this.restTemplate = new RestTemplate();
        this.diagnosisApiUrl = diagnosisApiUrl;
    }

    // GET all diagnoses from external API
    public Diagnosis[] getAllDiagnoses() {

        try {

            return restTemplate.getForObject(
                    diagnosisApiUrl + "/diagnosis",
                    Diagnosis[].class
            );

        } catch (RestClientException exception) {

            throw new ExternalApiException(
                    "Unable to connect to the diagnosis API"
            );
        }
    }

    // POST diagnosis to external API
    public Diagnosis createDiagnosis(
            Diagnosis diagnosis) {

        try {

            return restTemplate.postForObject(
                    diagnosisApiUrl + "/diagnosis",
                    diagnosis,
                    Diagnosis.class
            );

        } catch (RestClientException exception) {

            throw new ExternalApiException(
                    "Unable to create diagnosis"
            );
        }
    }
}