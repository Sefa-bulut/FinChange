package com.example.finchange.marketSimulation.service.impl;

import com.example.finchange.marketSimulation.client.ExternalAssetClient;
import com.example.finchange.marketSimulation.client.dto.ExternalApiResponseDto;
import com.example.finchange.marketSimulation.client.dto.ExternalAssetDto;
import com.example.finchange.marketSimulation.service.AssetMatchingService;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetMatchingServiceImpl implements AssetMatchingService {

    private final ExternalAssetClient externalAssetClient;
    private final AssetRepository assetRepository; // Doğrudan Repository ile konuşuyoruz.
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void findAndWriteAllMatchesToRedis() {
        log.info("===== Toplu Varlık Eşleştirme ve Redis'e Yazma Süreci Başladı =====");

        Map<String, ExternalAssetDto> externalStocksMap = fetchAndPrepareExternalData();
        if (externalStocksMap.isEmpty()) {
            log.error("Dış servis verileri alınamadı veya filtrelenecek uygun varlık bulunamadı. İşlem durduruldu.");
            return;
        }

        List<Asset> myAssets = assetRepository.findAll();
        log.info("Veritabanında {} adet varlık bulundu. Eşleşmeler kontrol ediliyor...", myAssets.size());

        int matchCount = 0;
        for (Asset asset : myAssets) {
            if (externalStocksMap.containsKey(asset.getBistCode())) {
                matchCount++;
                log.info("✅ EŞLEŞME BULUNDU: Yerel Varlık -> {}", asset.getBistCode());
                ExternalAssetDto externalAsset = externalStocksMap.get(asset.getBistCode());
                writeToRedis(asset, externalAsset);
            } else {
                log.warn("❌ EŞLEŞME YOK: Yerel varlık '{}' dış serviste bulunamadı veya aktif bir hisse senedi değil.", asset.getBistCode());
            }
        }

        log.info("===== Toplu Süreç Tamamlandı: Toplam {} eşleşme Redis'e yazıldı. =====", matchCount);
    }

    @Override
    public void findAndWriteSingleMatchToRedis(String bistCode) {
        log.info("Tekil varlık için Redis güncelleme süreci başlatıldı: {}", bistCode);

        // Not: Bu metodu daha verimli hale getirmek için, tüm listeyi çekmek yerine
        // dış API'nin tek bir varlığı getiren bir endpoint'i çağrılabilir.
        // Şimdilik mevcut yapıyı kullanıyoruz.
        Map<String, ExternalAssetDto> externalStocksMap = fetchAndPrepareExternalData();
        if (!externalStocksMap.containsKey(bistCode)) {
            log.warn("Tekil güncelleme için {} dış serviste bulunamadı veya aktif bir hisse değil. Redis'e yazılamadı.", bistCode);
            // İsteğe bağlı olarak, Redis'teki mevcut kaydı da silebiliriz.
            // redisTemplate.delete("asset:info:" + bistCode);
            return;
        }

        assetRepository.findByBistCode(bistCode).ifPresentOrElse(
                asset -> {
                    ExternalAssetDto externalAsset = externalStocksMap.get(bistCode);
                    writeToRedis(asset, externalAsset);
                },
                () -> log.error("Veritabanında BIST kodu '{}' olan varlık bulunamadı.", bistCode)
        );
    }

    /**
     * Dış servisten tüm hisse tanımlarını çeker, filtreler ve hızlı erişim için bir Map'e dönüştürür.
     * Bu metot private'tır çünkü bu sınıfın iç mantığıdır.
     */
    private Map<String, ExternalAssetDto> fetchAndPrepareExternalData() {
        ExternalApiResponseDto externalResponse = externalAssetClient.fetchAllAssetDefinitions();
        if (externalResponse == null || externalResponse.getHisseTanimList() == null) {
            return Collections.emptyMap();
        }

        return externalResponse.getHisseTanimList().stream()
                .map(map -> objectMapper.convertValue(map, ExternalAssetDto.class))
                .filter(dto -> "Stock".equalsIgnoreCase(dto.getSecurityType()) && "ACTIVE".equalsIgnoreCase(dto.getStatus()))
                .collect(Collectors.toMap(ExternalAssetDto::getCode, dto -> dto, (first, second) -> first));
    }

    /**
     * Verilen yerel ve dış varlık bilgilerini birleştirerek Redis'e bir Hash olarak yazar.
     */
    private void writeToRedis(Asset asset, ExternalAssetDto externalAsset) {
        try {
            Map<String, String> dataForRedis = createHybridDataMap(asset, externalAsset);
            String redisKey = "asset:info:" + asset.getBistCode();

            redisTemplate.opsForHash().putAll(redisKey, dataForRedis);
            redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

            log.info(" -> {} için hibrit kimlik bilgileri Redis'e başarıyla yazıldı.", asset.getBistCode());
        } catch (Exception e) {
            log.error(" -> {} için Redis'e yazma sırasında bir hata oluştu: {}", asset.getBistCode(), e.getMessage());
        }
    }

    /**
     * İki farklı kaynaktan gelen bilgiyi birleştirerek Redis'e yazılacak "hibrit" veri haritasını oluşturur.
     */
    private Map<String, String> createHybridDataMap(Asset asset, ExternalAssetDto externalAsset) {
        Map<String, String> dataMap = new HashMap<>();

        // Sizin sisteminizden gelenler (Size özel kurallar ve sınıflandırmalar)
        dataMap.put("bistCode", asset.getBistCode());
        dataMap.put("internalSector", asset.getSector());
        BigDecimal maxOrder = asset.getMaxOrderValue();
        dataMap.put("maxOrderValue", maxOrder != null ? maxOrder.toPlainString() : "0");

        // Dış API'den gelenler (Resmi ve doğrulanmış bilgiler)
        dataMap.put("companyName", externalAsset.getCompanyName());
        dataMap.put("isinCode", externalAsset.getIsinCode());
        dataMap.put("currency", externalAsset.getCurrency());
        dataMap.put("securityType", externalAsset.getSecurityType());

        return dataMap;
    }
}