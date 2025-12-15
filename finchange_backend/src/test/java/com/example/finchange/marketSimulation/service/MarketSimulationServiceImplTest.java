package com.example.finchange.marketSimulation.service;

import com.example.finchange.marketSimulation.service.MarketSessionService;
import com.example.finchange.marketSimulation.service.impl.MarketSimulationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketSimulationServiceImpl Unit Tests")
class MarketSimulationServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private MarketSessionService marketSessionService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MarketSimulationServiceImpl marketSimulationService;

    @Test
    @DisplayName("simulatePriceMovement - Piyasa kapalı, override yok")
    void simulatePriceMovement_MarketClosedNoOverride() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(marketSessionService.isMarketOpenNow()).thenReturn(false);
        when(valueOperations.get("market:override:simulation")).thenReturn(null);

        // When
        marketSimulationService.simulatePriceMovement();

        // Then
        verify(marketSessionService, times(1)).isMarketOpenNow();
        verify(valueOperations, times(1)).get("market:override:simulation");
        verify(redisTemplate, never()).keys(anyString());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("simulatePriceMovement - Simüle edilecek varlık yok")
    void simulatePriceMovement_NoAssetsToSimulate() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOperations.get("market:override:simulation")).thenReturn(null);
        when(redisTemplate.keys("asset:ohlc:*")).thenReturn(Set.of());

        // When
        marketSimulationService.simulatePriceMovement();

        // Then
        verify(marketSessionService, times(1)).isMarketOpenNow();
        verify(redisTemplate, times(1)).keys("asset:ohlc:*");
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("simulatePriceMovement - Redis keys null döndürür")
    void simulatePriceMovement_RedisKeysReturnsNull() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOperations.get("market:override:simulation")).thenReturn(null);
        when(redisTemplate.keys("asset:ohlc:*")).thenReturn(null);

        // When
        marketSimulationService.simulatePriceMovement();

        // Then
        verify(marketSessionService, times(1)).isMarketOpenNow();
        verify(redisTemplate, times(1)).keys("asset:ohlc:*");
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}
