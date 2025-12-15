package com.example.finchange.marketSimulation.service;

import com.example.finchange.marketSimulation.client.ExternalAssetClient;
import com.example.finchange.marketSimulation.client.dto.ExternalApiResponseDto;
import com.example.finchange.marketSimulation.client.dto.ExternalAssetDto;
import com.example.finchange.marketSimulation.service.impl.AssetMatchingServiceImpl;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetMatchingServiceImpl Unit Tests")
class AssetMatchingServiceImplTest {

    @Mock
    private ExternalAssetClient externalAssetClient;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private AssetMatchingServiceImpl assetMatchingService;

    private Asset testAsset;
    private ExternalAssetDto testExternalAsset;
    private ExternalApiResponseDto testApiResponse;

    @BeforeEach
    void setUp() {
        // Test Asset
        testAsset = new Asset();
        testAsset.setBistCode("AKBNK");
        testAsset.setSector("Banking");
        testAsset.setMaxOrderValue(new BigDecimal("1000000"));

        // Test External Asset
        testExternalAsset = new ExternalAssetDto();
        testExternalAsset.setCode("AKBNK");
        testExternalAsset.setCompanyName("Akbank T.A.Ş.");
        testExternalAsset.setIsinCode("TRABANK00017");
        testExternalAsset.setCurrency("TRY");
        testExternalAsset.setSecurityType("Stock");
        testExternalAsset.setStatus("ACTIVE");

        // Test API Response
        testApiResponse = new ExternalApiResponseDto();
        Map<String, Object> assetMap = new HashMap<>();
        assetMap.put("code", "AKBNK");
        assetMap.put("companyName", "Akbank T.A.Ş.");
        assetMap.put("isinCode", "TRABANK00017");
        assetMap.put("currency", "TRY");
        assetMap.put("securityType", "Stock");
        assetMap.put("status", "ACTIVE");
        testApiResponse.setHisseTanimList(List.of(assetMap));
    }

    @Test
    @DisplayName("findAndWriteAllMatchesToRedis - Dış API null response")
    void findAndWriteAllMatchesToRedis_ExternalApiNullResponse() {
        // Given
        when(externalAssetClient.fetchAllAssetDefinitions()).thenReturn(null);

        // When
        assetMatchingService.findAndWriteAllMatchesToRedis();

        // Then
        verify(externalAssetClient, times(1)).fetchAllAssetDefinitions();
        verify(assetRepository, never()).findAll();
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("findAndWriteAllMatchesToRedis - Dış API boş liste")
    void findAndWriteAllMatchesToRedis_ExternalApiEmptyList() {
        // Given
        ExternalApiResponseDto emptyResponse = new ExternalApiResponseDto();
        emptyResponse.setHisseTanimList(null);
        when(externalAssetClient.fetchAllAssetDefinitions()).thenReturn(emptyResponse);

        // When
        assetMatchingService.findAndWriteAllMatchesToRedis();

        // Then
        verify(externalAssetClient, times(1)).fetchAllAssetDefinitions();
        verify(assetRepository, never()).findAll();
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("findAndWriteSingleMatchToRedis - Dış serviste bulunamayan varlık")
    void findAndWriteSingleMatchToRedis_AssetNotFoundInExternalService() {
        // Given
        when(externalAssetClient.fetchAllAssetDefinitions()).thenReturn(testApiResponse);
        when(objectMapper.convertValue(any(Map.class), eq(ExternalAssetDto.class))).thenReturn(testExternalAsset);

        // When
        assetMatchingService.findAndWriteSingleMatchToRedis("NONEXIST");

        // Then
        verify(externalAssetClient, times(1)).fetchAllAssetDefinitions();
        verify(assetRepository, never()).findByBistCode(anyString());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("findAndWriteSingleMatchToRedis - Veritabanında bulunamayan varlık")
    void findAndWriteSingleMatchToRedis_AssetNotFoundInDatabase() {
        // Given
        when(externalAssetClient.fetchAllAssetDefinitions()).thenReturn(testApiResponse);
        when(objectMapper.convertValue(any(Map.class), eq(ExternalAssetDto.class))).thenReturn(testExternalAsset);
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(Optional.empty());

        // When
        assetMatchingService.findAndWriteSingleMatchToRedis("AKBNK");

        // Then
        verify(externalAssetClient, times(1)).fetchAllAssetDefinitions();
        verify(assetRepository, times(1)).findByBistCode("AKBNK");
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }
}
