package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCleanupServiceImpl {

    private final OrderRepository orderRepository;
    private final PortfolioService portfolioService;

    @Scheduled(cron = "0 10 18 * * MON-FRI", zone = "Europe/Istanbul")
    @Transactional
    public void cancelOpenOrdersAtDayEnd() {
        log.info("===== Gün Sonu Emir Temizleme Görevi Başladı =====");

        List<OrderStatus> statusesToCancel = List.of(OrderStatus.QUEUED, OrderStatus.ACTIVE, OrderStatus.PARTIALLY_FILLED);
        List<Order> openOrders = orderRepository.findByStatusIn(statusesToCancel);

        if (openOrders.isEmpty()) {
            log.info("Gün sonunda açık emir bulunamadı. Görev tamamlandı.");
            return;
        }

        log.info("{} adet gün sonu açık emir bulundu. İptal işlemleri başlatılıyor...", openOrders.size());

        for (Order order : openOrders) {
            try {
                portfolioService.releaseBlockForCancelledOrder(order);

                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                log.info("Emir ID {} gün sonunda başarıyla iptal edildi.", order.getId());

            } catch (Exception e) {
                log.error("KRİTİK HATA: Emir ID {} gün sonunda iptal edilirken bir hata oluştu! Hata: {}", order.getId(), e.getMessage(), e);
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
            }
        }

        log.info("===== Gün Sonu Emir Temizleme Görevi Tamamlandı =====");
    }
}