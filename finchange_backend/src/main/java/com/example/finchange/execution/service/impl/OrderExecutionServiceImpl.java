package com.example.finchange.execution.service.impl;

import com.example.finchange.execution.dto.event.OrderExecutedEvent;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.publisher.OrderEventPublisher;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.execution.service.ComissionService;
import com.example.finchange.execution.service.OrderExecutionService;
import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.portfolio.service.PortfolioService;
import com.example.finchange.marketSimulation.service.MarketSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.model.Asset;
import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderExecutionServiceImpl implements OrderExecutionService {

    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;
    private final ComissionService comissionService;
    private final OrderEventPublisher orderEventPublisher;
    private final BusinessDayCalculator businessDayCalculator;
    private final PortfolioService portfolioService;
    private final MarketSessionService marketSessionService;
    private final CustomerAccountRepository customerAccountRepository;
    private final AssetRepository assetRepository;

    @Override
    @Transactional
    public void executeSingleOrder(Order order, BigDecimal executionPrice) {

        try {
            int remainingLots = order.getInitialLotAmount() - order.getFilledLotAmount();
            if (remainingLots <= 0) return;
            
            int lotsToExecute = remainingLots; 

            BigDecimal totalValue = executionPrice.multiply(new BigDecimal(lotsToExecute));
            BigDecimal commissionAmount = comissionService.calculateCommission(totalValue);

            OrderExecution execution = createExecutionRecord(order, executionPrice, commissionAmount, lotsToExecute);
            orderExecutionRepository.save(execution);

            order.setFilledLotAmount(order.getFilledLotAmount() + lotsToExecute);
            order.setStatus(order.getFilledLotAmount() >= order.getInitialLotAmount() ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
            orderRepository.save(order);

            log.info("BAŞARILI: Emir ID {} için {} lot gerçekleşti.", order.getId(), lotsToExecute);

            try {

                boolean isSettlementControlsActive = marketSessionService.areSettlementControlsActive();

                if (order.getTransactionType() == TransactionType.BUY) {
                    if (isSettlementControlsActive) {
                        portfolioService.blockAssetForBuyExecution(execution);
                    } else {
                        portfolioService.increaseHoldingsImmediately(execution);
                    }
                } else if (order.getTransactionType() == TransactionType.SELL) {
                    if (isSettlementControlsActive) {
                        portfolioService.applySellExecutionHold(execution);
                    } else {
                        portfolioService.settleSellTransaction(execution);
                    }
                }
            } catch (Exception err) {
                log.error("Execution sonrası settlement override uygulaması sırasında hata: {}", err.getMessage(), err);
            }

            publishOrderEvent(order, execution);

        } catch (Exception e) {
            log.error("HATA: Emir ID {} işlenemedi! Sebep: {}. Emir 'FAILED' olarak işaretleniyor.", order.getId(), e.getMessage(), e);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }

    private OrderExecution createExecutionRecord(Order order, BigDecimal price, BigDecimal commission, int executedLots) {
        BigDecimal locked = (order.getOrderType() == OrderType.MARKET)
                ? price
                : order.getLimitPrice();

        return OrderExecution.builder()
                .order(order)
                .executedPrice(price)
                .lockedPrice(locked)
                .commissionAmount(commission)
                .executedLotAmount(executedLots)
                .executionTimestamp(LocalDateTime.now())
                .isSettled(false)
                .settlementDate(businessDayCalculator.getBusinessDayAfter(LocalDateTime.now().toLocalDate(), 2))
                .build();
    }

    private void publishOrderEvent(Order order, OrderExecution execution) {
        int remainingLots = order.getInitialLotAmount() - order.getFilledLotAmount();

        CustomerAccount acc = customerAccountRepository.findById(order.getCustomerAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Müşteri hesabı bulunamadı: " + order.getCustomerAccountId()));
        Integer customerId = acc.getCustomer().getId();
        String musteriKodu = acc.getCustomer().getCustomerCode();

        Asset asset = assetRepository.findById(order.getAssetId())
                .orElseThrow(() -> new EntityNotFoundException("Varlık bulunamadı: " + order.getAssetId()));
        String bistCode = asset.getBistCode();

        OrderExecutedEvent event = new OrderExecutedEvent(
                order.getId(),
                customerId,
                musteriKodu,
                order.getAssetId(),
                bistCode,
                order.getTransactionType().name(),
                order.getStatus().name(),
                execution.getExecutedLotAmount(),
                execution.getExecutedPrice(),
                execution.getCommissionAmount(),
                remainingLots,
                Instant.now()
        );
        orderEventPublisher.publishOrderExecutedEvent(event);
    }
}
