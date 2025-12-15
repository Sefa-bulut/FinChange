package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueuedOrderProcessor {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 40 9 * * MON-FRI", zone = "Europe/Istanbul")
    @Transactional
    public void processQueuedOrders() {
        log.info("===== [ZAMANLANMIŞ GÖREV] Kuyruktaki Emirleri İşleme (09:40) Başladı =====");
        
        List<Order> queuedOrders = orderRepository.findByStatusIn(List.of(OrderStatus.QUEUED));

        if (queuedOrders.isEmpty()) {
            log.info("Piyasaya gönderilecek kuyrukta emir bulunamadı. Görev sonlandırılıyor.");
            return;
        }

        log.info("{} adet emir kuyruktan alınıp piyasaya (ACTIVE) iletiliyor...", queuedOrders.size());
        
        for (Order order : queuedOrders) {
            order.setStatus(OrderStatus.ACTIVE);
        }
        
        orderRepository.saveAll(queuedOrders);
        log.info("===== [ZAMANLANMIŞ GÖREV] Kuyruktaki Emirleri İşleme Görevi Tamamlandı =====");
    }
}
