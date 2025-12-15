package com.example.finchange;

import com.example.finchange.marketSimulation.service.AssetMatchingService;
import com.example.finchange.marketSimulation.service.impl.DataCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

    private final AssetMatchingService matchingService;
    private final DataCollectorService dataCollectorService;

    @Override
    public void run(String... args) throws Exception {
        log.info("!!! TEST RUNNER BAŞLATILDI: Simülasyon ortamı hazırlanıyor... !!!");

        log.info("--- Adım 1: AssetMatchingService çalıştırılıyor... ---");
        matchingService.findAndWriteAllMatchesToRedis();
        log.info("--- Adım 1 Tamamlandı. ---");
        log.info("--- Adım 2: DataCollectorService çalıştırılıyor... ---");
        dataCollectorService.collectAndStoreDailyOhlcData();
        log.info("--- Adım 2 Tamamlandı. ---");

        log.info("!!! Simülasyon ortamı hazır. MarketSimulationService'in devreye girmesi bekleniyor... !!!");
    }
}