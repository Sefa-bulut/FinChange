package com.example.finchange.marketSimulation.service.impl;

import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.marketSimulation.service.MarketSessionService;
import com.example.finchange.operation.service.SystemDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketSessionServiceImpl implements MarketSessionService {

    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 55);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(18, 5);

    private final BusinessDayCalculator businessDayCalculator;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isMarketOpenNow() {
        try {
            String tradingOverride = redisTemplate.opsForValue().get("market:override:trading");
            if ("true".equalsIgnoreCase(tradingOverride)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        ZonedDateTime now = ZonedDateTime.now(ISTANBUL_ZONE);
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;

        LocalDate today = now.toLocalDate();
        if (!businessDayCalculator.isBusinessDay(today)) return false;

        LocalTime time = now.toLocalTime();
        return !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);
    }

    @Override
    public boolean areSettlementControlsActive() {
        try {
            String settlementOverride = redisTemplate.opsForValue().get("market:override:settlement_controls");
            // Varsayılan: kontroller AKTİF. Sadece "false" ise devre dışı.
            return !"false".equalsIgnoreCase(settlementOverride);
        } catch (Exception e) {
            log.error("Redis'ten takas kontrol durumu okunurken hata! Güvenlik amacıyla kontroller AKTİF varsayılıyor.", e);
            return true;
        }
    }
}


