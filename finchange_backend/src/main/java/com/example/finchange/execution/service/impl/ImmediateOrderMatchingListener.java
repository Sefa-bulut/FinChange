package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.events.OrdersCreatedEvent;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.execution.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImmediateOrderMatchingListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Async
    @TransactionalEventListener
    public void handleOrdersCreated(OrdersCreatedEvent event) {
        if (event == null || event.getCreatedOrderIds() == null || event.getCreatedOrderIds().isEmpty()) {
            return;
        }
        log.info("Yeni oluşturulan {} adet emir ID'si için (TRANSACTION SONRASI) anında eşleştirme listener'ı tetiklendi.", event.getCreatedOrderIds().size());
        try {
            List<Order> ordersToMatch = orderRepository.findByIdInWithAsset(event.getCreatedOrderIds());
            if (ordersToMatch != null && !ordersToMatch.isEmpty()) {
                orderService.tryToMatchOrdersImmediately(ordersToMatch);
            }
        } catch (Exception e) {
            log.error("Anında eşleştirme listener'ı çalışırken hata oluştu.", e);
        }
    }
}
