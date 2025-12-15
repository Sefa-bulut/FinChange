package com.example.finchange.marketSimulation.client;
import com.example.finchange.marketSimulation.client.dto.ExternalApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ExternalAssetClient {

    private final RestClient restClient;

    @Value("${external.api.url}")
    private String externalApiUrl;

    @Value("${external.api.key}")
    private String apiKey;

    public ExternalApiResponseDto fetchAllAssetDefinitions() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        return restClient.exchange(
                externalApiUrl,
                HttpMethod.GET,
                headers,
                ExternalApiResponseDto.class
        );
    }
}