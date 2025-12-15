package com.example.finchange.marketSimulation.service.impl;

import com.example.finchange.marketSimulation.dto.AssetDetailResponse;
import com.example.finchange.marketSimulation.dto.MarketDataResponse;
import com.example.finchange.marketSimulation.mapper.MarketDataMapper;
import com.example.finchange.marketSimulation.service.MarketDataService;
import com.example.finchange.operation.service.SystemDateService;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;




@Service
@RequiredArgsConstructor
public class MarketDataServiceImpl implements MarketDataService {

    private static final String ACTIVE_SIMULATION_KEY = "active_simulation_assets";
    private final StringRedisTemplate redisTemplate;
    private final AssetRepository assetRepository;
    private final SystemDateService systemDateService;

    @Override
    public MarketDataResponse getActiveAssetsWithInitialPrices() {
        LocalDateTime systemDateTime = systemDateService.getSystemDateTime();
        Set<String> ohlcKeys = redisTemplate.keys("asset:ohlc:*");
        if (ohlcKeys == null || ohlcKeys.isEmpty()) {
            return new MarketDataResponse(systemDateTime, Collections.emptyList());
        }

        List<Map<String, Object>> assets = ohlcKeys.stream()
                .map(key -> {
                    String bistCode = key.substring("asset:ohlc:".length());
                    Map<Object, Object> ohlcData = redisTemplate.opsForHash().entries(key);
                    return MarketDataMapper.mapOhlcDataToAssetMap(bistCode, ohlcData);
                })
                .collect(Collectors.toList());

        return new MarketDataResponse(systemDateTime, assets);
    }


    @Override
    public AssetDetailResponse getAssetDetails(String bistCode) {
        String upperBistCode = bistCode.toUpperCase();

        Asset asset = assetRepository.findByBistCode(upperBistCode)
            .orElseThrow(() -> new EntityNotFoundException("Varlık bulunamadı: " + upperBistCode));

        Map<Object, Object> ohlcData = redisTemplate.opsForHash().entries("asset:ohlc:" + upperBistCode);
        if (ohlcData.isEmpty()) {
            return AssetDetailResponse.builder()
                .bistCode(asset.getBistCode())
                .companyName(asset.getCompanyName())
                .currency(asset.getCurrency())
                .build();
        }

        String livePriceStr = redisTemplate.opsForValue().get("asset:live_price:" + upperBistCode);
        BigDecimal livePrice = (livePriceStr != null) ? new BigDecimal(livePriceStr) : null;

        return AssetDetailResponse.builder()
                .bistCode(asset.getBistCode())
                .companyName(asset.getCompanyName())
                .currency(asset.getCurrency()) // <<<=== BU SATIRI EKLE
                .previousClose(new BigDecimal((String) ohlcData.get("previousClose")))
                .dailyHigh(new BigDecimal((String) ohlcData.get("dailyHigh")))
                .dailyLow(new BigDecimal((String) ohlcData.get("dailyLow")))
                .openPrice(new BigDecimal((String) ohlcData.get("openPrice")))
                .livePrice(livePrice)
                .build();
    }
}
