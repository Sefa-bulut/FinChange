package com.example.finchange.marketSimulation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.finchange.marketSimulation.client.PriceDataClient;
import com.example.finchange.marketSimulation.client.dto.OhlcDataDto;
import com.example.finchange.marketSimulation.client.dto.PriceApiResponseDto;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataCollectorService {


    private final StringRedisTemplate redisTemplate;
    private final PriceDataClient priceDataClient;



    @Scheduled(cron = "0 45 * * * *", zone = "Europe/Istanbul")
    public void collectAndStoreDailyOhlcData() {
        log.info("===== Günlük OHLC Veri Toplama Görevi Başladı =====");

        Set<String> assetInfoKeys = fetchAssetKeysFromRedis();
        if (assetInfoKeys.isEmpty()) {
            log.warn("Redis'te 'asset:info:*' cache'inde hiç varlık bulunamadı. Görev sonlandırılıyor.");
            return;
        }
        log.info("{} adet kayıtlı varlık için fiyat verisi çekilecek.", assetInfoKeys.size());


        for (String key : assetInfoKeys) {
            String bistCode = key.substring("asset:info:".length());

            try {
                PriceApiResponseDto apiResponse = priceDataClient.fetchOhlcDataForAsset(bistCode);

                OhlcDataDto ohlcData = apiResponse.getOhlcData();

                if (ohlcData != null) {
                    writeOhlcDataToRedis(bistCode, ohlcData);
                } else {
                    log.warn(" -> {} için Fiyat API'sinden veri alınamadı.", bistCode);
                }

            } catch (Exception e) {
                log.error(" -> {} için veri çekme veya işleme sırasında bir hata oluştu: {}", bistCode, e.getMessage());
            }
        }

        log.info("===== Günlük OHLC Veri Toplama Görevi Tamamlandı =====");
    }


    private Set<String> fetchAssetKeysFromRedis() {
        return redisTemplate.keys("asset:info:*");
    }



    private void writeOhlcDataToRedis(String bistCode, OhlcDataDto ohlcData) {
        String redisKey = "asset:ohlc:" + bistCode;

        Map<String, String> dataForRedis = new HashMap<>();
        dataForRedis.put("previousClose", String.valueOf(ohlcData.getClosePrice()));
        dataForRedis.put("dailyHigh", String.valueOf(ohlcData.getHighPrice()));
        dataForRedis.put("dailyLow", String.valueOf(ohlcData.getLowPrice()));
        dataForRedis.put("openPrice", String.valueOf(ohlcData.getOpenPrice()));
        dataForRedis.put("dataDate", ohlcData.getDataDate());

        redisTemplate.opsForHash().putAll(redisKey, dataForRedis);

        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);

        log.info(" -> {} için OHLC verileri Redis'e başarıyla yazıldı. Kapanış: {}", bistCode, ohlcData.getClosePrice());
    }
}