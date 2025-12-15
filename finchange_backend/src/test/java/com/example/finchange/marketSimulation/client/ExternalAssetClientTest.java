package com.example.finchange.marketSimulation.client;

import com.example.finchange.marketSimulation.client.dto.ExternalApiResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalAssetClient Unit Tests")
class ExternalAssetClientTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private ExternalAssetClient externalAssetClient;

    private ExternalApiResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(externalAssetClient, "externalApiUrl", "https://api.example.com/assets");
        ReflectionTestUtils.setField(externalAssetClient, "apiKey", "test-api-key");

        // Create mock response
        mockResponse = new ExternalApiResponseDto();
        Map<String, Object> assetData = Map.of(
                "code", "AKBNK",
                "companyName", "Akbank T.A.Ş.",
                "securityType", "Stock",
                "status", "ACTIVE"
        );
        mockResponse.setHisseTanimList(List.of(assetData));
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - Başarılı API çağrısı")
    void fetchAllAssetDefinitions_SuccessfulApiCall() {
        // Given
        when(restClient.exchange(
                eq("https://api.example.com/assets"),
                eq(HttpMethod.GET),
                any(HttpHeaders.class),
                eq(ExternalApiResponseDto.class)
        )).thenReturn(mockResponse);

        // When
        ExternalApiResponseDto result = externalAssetClient.fetchAllAssetDefinitions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHisseTanimList()).hasSize(1);
        assertThat(result.getHisseTanimList().get(0)).containsEntry("code", "AKBNK");

        verify(restClient, times(1)).exchange(
                eq("https://api.example.com/assets"),
                eq(HttpMethod.GET),
                argThat(headers -> "test-api-key".equals(headers.getFirst("x-api-key"))),
                eq(ExternalApiResponseDto.class)
        );
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - API key header doğru şekilde set edilir")
    void fetchAllAssetDefinitions_ApiKeyHeaderSetCorrectly() {
        // Given
        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenReturn(mockResponse);

        // When
        externalAssetClient.fetchAllAssetDefinitions();

        // Then
        verify(restClient, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                argThat(headers -> {
                    String apiKeyHeader = headers.getFirst("x-api-key");
                    return "test-api-key".equals(apiKeyHeader);
                }),
                any(Class.class)
        );
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - RestClient exception durumu")
    void fetchAllAssetDefinitions_RestClientException() {
        // Given
        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        try {
            externalAssetClient.fetchAllAssetDefinitions();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("API connection failed");
        }

        verify(restClient, times(1)).exchange(
                eq("https://api.example.com/assets"),
                eq(HttpMethod.GET),
                any(HttpHeaders.class),
                eq(ExternalApiResponseDto.class)
        );
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - Null response handling")
    void fetchAllAssetDefinitions_NullResponse() {
        // Given
        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenReturn(null);

        // When
        ExternalApiResponseDto result = externalAssetClient.fetchAllAssetDefinitions();

        // Then
        assertThat(result).isNull();
        verify(restClient, times(1)).exchange(
                eq("https://api.example.com/assets"),
                eq(HttpMethod.GET),
                any(HttpHeaders.class),
                eq(ExternalApiResponseDto.class)
        );
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - Empty response handling")
    void fetchAllAssetDefinitions_EmptyResponse() {
        // Given
        ExternalApiResponseDto emptyResponse = new ExternalApiResponseDto();
        emptyResponse.setHisseTanimList(List.of());
        
        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenReturn(emptyResponse);

        // When
        ExternalApiResponseDto result = externalAssetClient.fetchAllAssetDefinitions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHisseTanimList()).isEmpty();
        verify(restClient, times(1)).exchange(
                eq("https://api.example.com/assets"),
                eq(HttpMethod.GET),
                any(HttpHeaders.class),
                eq(ExternalApiResponseDto.class)
        );
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - Multiple assets response")
    void fetchAllAssetDefinitions_MultipleAssetsResponse() {
        // Given
        Map<String, Object> asset1 = Map.of(
                "code", "AKBNK",
                "companyName", "Akbank T.A.Ş.",
                "securityType", "Stock",
                "status", "ACTIVE"
        );
        Map<String, Object> asset2 = Map.of(
                "code", "THYAO",
                "companyName", "Türk Hava Yolları A.O.",
                "securityType", "Stock",
                "status", "ACTIVE"
        );

        ExternalApiResponseDto multipleAssetsResponse = new ExternalApiResponseDto();
        multipleAssetsResponse.setHisseTanimList(List.of(asset1, asset2));

        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenReturn(multipleAssetsResponse);

        // When
        ExternalApiResponseDto result = externalAssetClient.fetchAllAssetDefinitions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHisseTanimList()).hasSize(2);
        assertThat(result.getHisseTanimList().get(0)).containsEntry("code", "AKBNK");
        assertThat(result.getHisseTanimList().get(1)).containsEntry("code", "THYAO");
    }

    @Test
    @DisplayName("fetchAllAssetDefinitions - Configuration values used correctly")
    void fetchAllAssetDefinitions_ConfigurationValuesUsedCorrectly() {
        // Given
        ReflectionTestUtils.setField(externalAssetClient, "externalApiUrl", "https://custom.api.com/data");
        ReflectionTestUtils.setField(externalAssetClient, "apiKey", "custom-key-123");

        when(restClient.exchange(anyString(), any(HttpMethod.class), any(HttpHeaders.class), any(Class.class)))
                .thenReturn(mockResponse);

        // When
        externalAssetClient.fetchAllAssetDefinitions();

        // Then
        verify(restClient, times(1)).exchange(
                eq("https://custom.api.com/data"),
                eq(HttpMethod.GET),
                argThat(headers -> "custom-key-123".equals(headers.getFirst("x-api-key"))),
                eq(ExternalApiResponseDto.class)
        );
    }
}
