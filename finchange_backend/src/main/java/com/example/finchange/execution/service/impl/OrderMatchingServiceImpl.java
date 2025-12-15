package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.execution.service.OrderExecutionService;
import com.example.finchange.execution.service.OrderMatchingService;
import com.example.finchange.marketSimulation.kafka.PriceUpdateEvent;
import com.example.finchange.portfolio.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderMatchingServiceImpl implements OrderMatchingService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final OrderExecutionService orderExecutionService; 

    @Override
    @KafkaListener(topics = "market-price-updates", groupId = "finchange-matching-engine", containerFactory = "kafkaListenerContainerFactory")
    @Transactional(readOnly = true) 
    public void handlePriceUpdate(PriceUpdateEvent event) {
        try {
            Integer assetId = assetRepository.findByBistCode(event.getAssetCode())
                    .map(asset -> asset.getId())
                    .orElse(null);

            if (assetId == null) {
                log.warn("{} için veritabanında varlık tanımı bulunamadı. Eşleştirme atlanıyor.", event.getAssetCode());
                return;
            }

            List<Order> buyOrders = orderRepository.findMatchingBuyOrders(assetId, event.getPrice());
            List<Order> sellOrders = orderRepository.findMatchingSellOrders(assetId, event.getPrice());

            if (buyOrders.isEmpty() && sellOrders.isEmpty()) {
                return;
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("{} için {} alım ve {} satım emri eşleşme için gönderiliyor.", event.getAssetCode(), buyOrders.size(), sellOrders.size());
                    buyOrders.forEach(order -> orderExecutionService.executeSingleOrder(order, event.getPrice()));
                    sellOrders.forEach(order -> orderExecutionService.executeSingleOrder(order, event.getPrice()));
                }
            });

        } catch (Exception e) {
            log.error("Kafka'dan gelen fiyat güncelleme mesajı işlenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}