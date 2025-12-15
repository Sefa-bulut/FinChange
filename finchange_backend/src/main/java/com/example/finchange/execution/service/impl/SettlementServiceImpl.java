package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.exception.SettlementFailedException;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.execution.service.SettlementService;
import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);
    private final BusinessDayCalculator businessDayCalculator;
    private final OrderExecutionRepository executionRepository;
    private final PortfolioService portfolioService;

    @Override
    @Scheduled(cron = "0 40 9 * * MON-FRI", zone = "Europe/Istanbul")
    public void performDailySettlement() {
        LocalDate settlementTargetDate = businessDayCalculator.getBusinessDayBefore(LocalDate.now(), 2);
        log.info("Günlük T+2 Takas Servisi başlatıldı. Hedef işlem tarihi: {}", settlementTargetDate);


        LocalDateTime startOfDay = settlementTargetDate.atStartOfDay();
        LocalDateTime endOfDay = settlementTargetDate.plusDays(1).atStartOfDay();

        List<OrderExecution> executionsToSettle = executionRepository.findUnsettledExecutionsByTimestampBetween(startOfDay, endOfDay);

        if (executionsToSettle.isEmpty()) {
            log.info("Bugün için takası yapılacak T+2 işlemi bulunamadı.");
            return;
        }

        log.info("{} adet T+2 işlemi takas için bulundu. Netleştirme başlıyor...", executionsToSettle.size());

        for (OrderExecution execution : executionsToSettle) {
            try {
                settleSingleTransaction(execution);
            } catch (SettlementFailedException e) {
                log.error("KRİTİK TAKAS HATASI! Bu T+2 işlemi atlandı. Detaylar: {}", e.getMessage());
            } catch (Exception e) {
                log.error("BEKLENMEDİK HATA! Takas işlemi atlandı. Execution ID: {}. Hata: {}", execution.getId(), e.getMessage(), e);
            }
        }
        log.info("Günlük T+2 Takas Servisi görevini tamamladı.");
    }

    @Transactional
    public void settleSingleTransaction(OrderExecution execution) {
        try {
            TransactionType transactionType = execution.getOrder().getTransactionType();

            if (TransactionType.BUY.equals(transactionType)) {
                portfolioService.settleBuyTransaction(execution);
            } else if (TransactionType.SELL.equals(transactionType)) {
                portfolioService.settleSellTransaction(execution);
            } else {
                throw new IllegalStateException("Geçersiz işlem tipiyle takas yapılamaz: " + transactionType);
            }

            execution.setSettled(true);
            executionRepository.save(execution);
            log.info("Takas başarılı: Execution ID {} netleştirildi.", execution.getId());

        } catch (Exception e) {
            throw new SettlementFailedException(
                    "Execution ID " + execution.getId() + " için takas başarısız oldu. Sebep: " + e.getMessage(), e
            );
        }
    }
}