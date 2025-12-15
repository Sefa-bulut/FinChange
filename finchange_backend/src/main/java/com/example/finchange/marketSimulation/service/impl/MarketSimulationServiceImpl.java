package com.example.finchange.marketSimulation.service.impl;

import com.example.finchange.marketSimulation.kafka.PriceUpdateEvent;
import com.example.finchange.marketSimulation.service.MarketSessionService; // YENİ IMPORT
import com.example.finchange.marketSimulation.service.MarketSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketSimulationServiceImpl implements MarketSimulationService {

    private static final String KAFKA_TOPIC = "market-price-updates";

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MarketSessionService marketSessionService; // YENİ: Merkezi seans servisini inject et

    @Override
    @Scheduled(fixedRate = 5000) // Her 5 saniyede bir çalışır
    public void simulatePriceMovement() {
        // 1. Merkezi servisten piyasanın açık olup olmadığını kontrol et.
        boolean isOpen = marketSessionService.isMarketOpenNow();
        boolean simulationOverride = false;
        try {
            String simOverrideStr = redisTemplate.opsForValue().get("market:override:simulation");
            simulationOverride = "true".equalsIgnoreCase(simOverrideStr);
        } catch (Exception ignored) {
            // Redis erişimi sorunluysa override'ı yok sayıp normal kurallara göre devam ederiz
        }
        if (!isOpen && !simulationOverride) {
            return; // Piyasa kapalı ve override yoksa çalışmayız.
        }

        // 2. Simüle edilecek varlıkları (OHLC verisi olanları) Redis'ten al.
        Set<String> assetsToSimulate = getAssetsToSimulate();
        if (assetsToSimulate.isEmpty()) {
            // Bu artık bir uyarı değil, normal bir durum olabilir (örn: sabah 09:45'ten önce)
            log.trace("Simüle edilecek OHLC verisine sahip hisse bulunamadı.");
            return;
        }

        log.debug("--- Piyasa Simülasyonu Tick Başladı ({} varlık için) ---", assetsToSimulate.size());

        for (String bistCode : assetsToSimulate) {
            try {
                simulatePriceForAsset(bistCode);
            } catch (Exception e) {
                log.error(" -> {} için simülasyon sırasında bir hata oluştu: {}", bistCode, e.getMessage());
            }
        }
    }

    private void simulatePriceForAsset(String bistCode) {
        Map<Object, Object> ohlcRules = redisTemplate.opsForHash().entries("asset:ohlc:" + bistCode);
        if (ohlcRules.isEmpty()) {
            log.debug(" -> {} için OHLC kuralları (asset:ohlc) bulunamadı. Simülasyon pas geçiliyor.", bistCode);
            return;
        }

        BigDecimal dailyHigh = new BigDecimal((String) ohlcRules.get("dailyHigh"));
        BigDecimal dailyLow = new BigDecimal((String) ohlcRules.get("dailyLow"));
        BigDecimal previousClose = new BigDecimal((String) ohlcRules.get("previousClose"));

        String livePriceStr = redisTemplate.opsForValue().get("asset:live_price:" + bistCode);
        BigDecimal currentPrice = (livePriceStr != null) ? new BigDecimal(livePriceStr) : previousClose;

        BigDecimal newPrice = generateNewPrice(currentPrice, dailyLow, dailyHigh);

        redisTemplate.opsForValue().set("asset:live_price:" + bistCode, newPrice.toPlainString());

        publishPriceUpdate(bistCode, newPrice);
    }

    private BigDecimal generateNewPrice(BigDecimal currentPrice, BigDecimal low, BigDecimal high) {
        double volatility = 0.005; // Fiyatın her adımda max %0.5 değişebileceğini varsayalım
        double changePercentage = (Math.random() - 0.5) * 2 * volatility;
        BigDecimal newPrice = currentPrice.multiply(BigDecimal.valueOf(1 + changePercentage));

        // Fiyatın gün içi en yüksek ve en düşük sınırları aşmamasını sağla
        if (newPrice.compareTo(high) > 0) newPrice = high;
        if (newPrice.compareTo(low) < 0) newPrice = low;

        return newPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private void publishPriceUpdate(String bistCode, BigDecimal newPrice) {
        PriceUpdateEvent event = new PriceUpdateEvent(bistCode, newPrice, Instant.now());
        kafkaTemplate.send(KAFKA_TOPIC, bistCode, event);
       // log.info(" -> Fiyat Güncellemesi Kafka'ya Yayınlandı: {} - {} TL", bistCode, newPrice);
    }
    private Set<String> getAssetsToSimulate() {
        Set<String> ohlcKeys = redisTemplate.keys("asset:ohlc:*");
        if (ohlcKeys == null) {
            return Set.of();
        }
        return ohlcKeys.stream()
                .map(key -> key.substring("asset:ohlc:".length()))
                .collect(Collectors.toSet());
    }
}