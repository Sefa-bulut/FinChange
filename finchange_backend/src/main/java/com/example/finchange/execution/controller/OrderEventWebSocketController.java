package com.example.finchange.execution.controller;

import com.example.finchange.execution.dto.event.OrderCancelledEvent;
import com.example.finchange.execution.dto.event.OrderExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderEventWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-events", groupId = "order-websocket-group")
    public void handleOrderEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            String destination = "/topic/order-events";
            messagingTemplate.convertAndSend(destination, event);
            
            if (event instanceof OrderExecutedEvent) {
                OrderExecutedEvent executedEvent = (OrderExecutedEvent) event;
                log.info("WebSocket: Order executed event gönderildi - Order ID: {}", executedEvent.orderId());
            } else if (event instanceof OrderCancelledEvent) {
                OrderCancelledEvent cancelledEvent = (OrderCancelledEvent) event;
                log.info("WebSocket: Order cancelled event gönderildi - Order ID: {}", cancelledEvent.orderId());
            } else {
                log.info("WebSocket: Order event gönderildi - Type: {}", event.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("Order event WebSocket'e gönderilirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}