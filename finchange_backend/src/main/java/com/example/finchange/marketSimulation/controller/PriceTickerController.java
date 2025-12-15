package com.example.finchange.marketSimulation.controller;


import com.example.finchange.marketSimulation.kafka.PriceUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PriceTickerController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "market-price-updates", groupId = "price-websocket-group")
    public void handlePriceUpdate(PriceUpdateEvent event) {
        try {
            String destination = "/topic/prices/" + event.getAssetCode();
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception e) {
            log.error("Fiyat güncellemesi işlenirken bir hata oluştu: {}", event, e);
            throw new RuntimeException("Kafka mesajı işlenemedi.", e);
        }
    }
}