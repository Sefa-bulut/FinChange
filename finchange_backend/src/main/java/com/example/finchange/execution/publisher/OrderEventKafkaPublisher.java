package com.example.finchange.execution.publisher;

import com.example.finchange.execution.dto.event.OrderCancelledEvent;
import com.example.finchange.execution.dto.event.OrderExecutedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventKafkaPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    @Override
    public void publishOrderExecutedEvent(OrderExecutedEvent event) {
        try {
            log.info("'{}' topic'ine yeni bir emir gerçekleşme olayı gönderiliyor: Order ID {}", ORDER_EVENTS_TOPIC, event.orderId());


            kafkaTemplate.send(ORDER_EVENTS_TOPIC, String.valueOf(event.orderId()), event);

            log.info("Olay başarıyla gönderildi: Order ID {}", event.orderId());
        } catch (Exception e) {
            log.error("'{}' topic'ine olay gönderilirken kritik hata oluştu! Event: {}", ORDER_EVENTS_TOPIC, event, e);
        }
    }

        @Override
    public void publishOrderCancelledEvent(OrderCancelledEvent event) {
        try {
            log.info("'{}' topic'ine yeni bir emir iptal olayı gönderiliyor: Order ID {}", ORDER_EVENTS_TOPIC, event.orderId());
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, String.valueOf(event.orderId()), event);
            log.info("İptal olayı başarıyla gönderildi: Order ID {}", event.orderId());
        } catch (Exception e) {
            log.error("'{}' topic'ine iptal olayı gönderilirken kritik hata oluştu! Event: {}", ORDER_EVENTS_TOPIC, event, e);
        }
    }
}