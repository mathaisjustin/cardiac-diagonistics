package com.elsevier.cardiac.diagnosis.service.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers this service with Eureka using its plain REST API directly,
 * bypassing Spring Cloud's client library - spring-cloud-starter-netflix-eureka-client
 * (and spring-cloud-commons underneath it) isn't binary-compatible with Spring Boot 4 yet
 * (it references a class Boot 4 renamed), so this avoids that entirely while still giving
 * every service instance a heartbeat-backed registration in Eureka.
 *
 * <p>See Eureka's REST API: register (POST), heartbeat (PUT), deregister (DELETE),
 * all under {@code /eureka/apps/{appName}[/{instanceId}]}.
 */
@Component
public class EurekaRegistrar {

    private static final Logger log = LoggerFactory.getLogger(EurekaRegistrar.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${eureka.url:http://localhost:8761/eureka}")
    private String eurekaUrl;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${eureka.instance-hostname:localhost}")
    private String hostname;

    @Value("${server.port}")
    private String port;

    private String instanceId;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {

        instanceId = hostname + ":" + appName.toLowerCase() + ":" + port;

        Map<String, Object> body = buildInstanceBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(
                    eurekaUrl + "/apps/" + appName.toUpperCase(),
                    new HttpEntity<>(body, headers),
                    Void.class
            );

            log.info("Registered with Eureka as {} (instanceId={})", appName.toUpperCase(), instanceId);

        } catch (RestClientException e) {
            log.error("Failed to register with Eureka at {}: {}", eurekaUrl, e.getMessage());
        }
    }

    @Scheduled(fixedRate = 30000, initialDelay = 30000)
    public void heartbeat() {

        if (instanceId == null) {
            return;
        }

        try {
            restTemplate.put(
                    eurekaUrl + "/apps/" + appName.toUpperCase() + "/" + instanceId,
                    null
            );

        } catch (RestClientException e) {
            log.warn("Eureka heartbeat failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void deregister() {

        if (instanceId == null) {
            return;
        }

        try {
            restTemplate.delete(
                    eurekaUrl + "/apps/" + appName.toUpperCase() + "/" + instanceId
            );

            log.info("Deregistered from Eureka: {}", instanceId);

        } catch (RestClientException e) {
            log.warn("Eureka deregistration failed: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildInstanceBody() {

        Map<String, Object> portInfo = new LinkedHashMap<>();
        portInfo.put("$", port);
        portInfo.put("@enabled", "true");

        Map<String, Object> dataCenterInfo = new LinkedHashMap<>();
        dataCenterInfo.put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo");
        dataCenterInfo.put("name", "MyOwn");

        Map<String, Object> instance = new LinkedHashMap<>();
        instance.put("instanceId", instanceId);
        instance.put("hostName", hostname);
        instance.put("app", appName.toUpperCase());
        instance.put("vipAddress", appName);
        instance.put("secureVipAddress", appName);
        instance.put("ipAddr", hostname);
        instance.put("status", "UP");
        instance.put("port", portInfo);
        instance.put("dataCenterInfo", dataCenterInfo);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("instance", instance);

        return body;
    }
}
