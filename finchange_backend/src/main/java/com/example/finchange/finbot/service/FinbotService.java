package com.example.finchange.finbot.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FinbotService {

    private final RestTemplate restTemplate;

    @Value("${finbot.base-url:http://localhost:8000}")
    private String finbotBaseUrl;

    public FinbotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> ask(String question, String language) {
        try {
            // FastAPI /ask expects application/x-www-form-urlencoded
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("question", question);
            form.add("language", language);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            final String url = finbotBaseUrl + "/ask";
            log.info("[Finbot] POST {} form={}", url, form);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    new ParameterizedTypeReference<>() {}
            );
            log.info("[Finbot] Response status={} bodyKeys={}", response.getStatusCode(),
                    response.getBody() != null ? response.getBody().keySet() : null);
            return response.getBody();
        } catch (RestClientResponseException e) {
            log.error("[Finbot][ask] HTTP error status={} body={} url={}", e.getRawStatusCode(), e.getResponseBodyAsString(), finbotBaseUrl + "/ask");
            throw e;
        } catch (RestClientException e) {
            log.error("[Finbot][ask] RestClientException: {} cause={}", e.getMessage(), e.getCause() != null ? e.getCause().toString() : null);
            throw e;
        }
    }

    public Map<String, Object> getTurkishNews() {
        try {
            final String url = finbotBaseUrl + "/turkish-news";
            log.info("[Finbot] GET {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("[Finbot] Response status={} bodyKeys={}", response.getStatusCode(),
                    response.getBody() != null ? response.getBody().keySet() : null);
            return response.getBody();
        } catch (RestClientResponseException e) {
            log.error("[Finbot][news] HTTP error status={} body={} url={}", e.getRawStatusCode(), e.getResponseBodyAsString(), finbotBaseUrl + "/turkish-news");
            throw e;
        } catch (RestClientException e) {
            log.error("[Finbot][news] RestClientException: {} cause={}", e.getMessage(), e.getCause() != null ? e.getCause().toString() : null);
            throw e;
        }
    }
}
