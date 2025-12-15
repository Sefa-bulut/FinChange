package com.example.finchange.marketSimulation.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestClient {

    private final RestTemplate restTemplate;


    public <T> T exchange(String url, HttpMethod method, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
        return response.getBody();
    }


    public <T, R> T exchange(String url, HttpMethod method, HttpHeaders headers, R requestBody, Class<T> responseType) {
        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
        return response.getBody();
    }
}