package com.example.finchange.marketSimulation.client;

import com.example.finchange.marketSimulation.client.dto.PriceApiResponseDto;
import com.example.finchange.operation.service.SystemDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PriceDataClient {

    private final RestClient restClient;
    private final SystemDateService systemDateService;

    @Value("${price.api.url}")
    private String priceApiBaseUrl;

    @Value("${price.api.key}")
    private String apiKey;

    public PriceApiResponseDto fetchOhlcDataForAsset(String bistCode) {

        String apiFormattedBistCode = bistCode + ".E";
        String systemDateString = systemDateService.getSystemDate().toString();
        String fullUrl = UriComponentsBuilder.fromHttpUrl(priceApiBaseUrl)
                .queryParam("asset_code", apiFormattedBistCode)
                .queryParam("data_date", systemDateString)
                .toUriString();


        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        return restClient.exchange(
                fullUrl,
                HttpMethod.GET,
                headers,
                PriceApiResponseDto.class
        );
    }
}