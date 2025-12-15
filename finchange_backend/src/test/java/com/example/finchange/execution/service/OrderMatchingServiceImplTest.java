package com.example.finchange.execution.service;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.service.impl.OrderMatchingServiceImpl;
import com.example.finchange.marketSimulation.kafka.PriceUpdateEvent;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.execution.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderMatchingServiceImpl - handlePriceUpdate tests")
class OrderMatchingServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private OrderExecutionService orderExecutionService;

    @InjectMocks
    private OrderMatchingServiceImpl service;

    @AfterEach
    void cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Asset bulunamazsa işlem yapılmaz")
    void assetNotFound_noActions() {
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(Optional.empty());

        service.handlePriceUpdate(new PriceUpdateEvent("AKBNK", new BigDecimal("10.00"), null));

        verifyNoInteractions(orderRepository, orderExecutionService);
    }

    @Test
    @DisplayName("Eşleşecek emir yoksa afterCommit olsa bile çağrı yapılmaz")
    void noMatchingOrders_noExecution() {
        Asset asset = new Asset(); asset.setId(1); asset.setBistCode("AKBNK");
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(Optional.of(asset));
        when(orderRepository.findMatchingBuyOrders(eq(1), any())).thenReturn(List.of());
        when(orderRepository.findMatchingSellOrders(eq(1), any())).thenReturn(List.of());

        TransactionSynchronizationManager.initSynchronization();
        service.handlePriceUpdate(new PriceUpdateEvent("AKBNK", new BigDecimal("10.00"), null));
        for (TransactionSynchronization sync : new ArrayList<>(TransactionSynchronizationManager.getSynchronizations())) {
            sync.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();

        verifyNoInteractions(orderExecutionService);
    }

    @Test
    @DisplayName("Eşleşen emirler afterCommit ile executeSingleOrder'a gönderilir")
    void matchingOrders_executeAfterCommit() {
        Asset asset = new Asset(); asset.setId(2); asset.setBistCode("THYAO");
        when(assetRepository.findByBistCode("THYAO")).thenReturn(Optional.of(asset));
        Order buy = new Order(); Order sell = new Order();
        when(orderRepository.findMatchingBuyOrders(eq(2), any())).thenReturn(List.of(buy));
        when(orderRepository.findMatchingSellOrders(eq(2), any())).thenReturn(List.of(sell));

        TransactionSynchronizationManager.initSynchronization();
        PriceUpdateEvent event = new PriceUpdateEvent("THYAO", new BigDecimal("123.45"), null);
        service.handlePriceUpdate(event);

        verify(orderExecutionService, never()).executeSingleOrder(any(), any());

        for (TransactionSynchronization sync : new ArrayList<>(TransactionSynchronizationManager.getSynchronizations())) {
            sync.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();

        verify(orderExecutionService, times(1)).executeSingleOrder(eq(buy), eq(new BigDecimal("123.45")));
        verify(orderExecutionService, times(1)).executeSingleOrder(eq(sell), eq(new BigDecimal("123.45")));
    }
}
