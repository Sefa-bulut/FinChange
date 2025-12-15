package com.example.finchange.execution.service;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.execution.service.impl.SettlementServiceImpl;
import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.portfolio.service.PortfolioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementServiceImpl tests")
class SettlementServiceImplTest {

    @Mock private BusinessDayCalculator businessDayCalculator;
    @Mock private OrderExecutionRepository executionRepository;
    @Mock private PortfolioService portfolioService;

    @InjectMocks
    private SettlementServiceImpl service;

    private OrderExecution buildExec(TransactionType type) {
        Order order = new Order();
        order.setTransactionType(type);
        OrderExecution ex = new OrderExecution();
        ex.setOrder(order);
        ex.setId(1L);
        ex.setSettled(false);
        ex.setExecutionTimestamp(LocalDateTime.now().minusDays(2));
        return ex;
    }

    @Test
    @DisplayName("performDailySettlement: hiç execution yoksa erken döner")
    void performDailySettlement_noExecutions() {
        when(businessDayCalculator.getBusinessDayBefore(any(LocalDate.class), eq(2)))
                .thenReturn(LocalDate.now().minusDays(2));
        when(executionRepository.findUnsettledExecutionsByTimestampBetween(any(), any())).thenReturn(List.of());

        service.performDailySettlement();
        verifyNoInteractions(portfolioService);
    }

    @Test
    @DisplayName("performDailySettlement: BUY ve SELL işlemleri işlenir, kaydedilir")
    void performDailySettlement_processesExecutions() {
        when(businessDayCalculator.getBusinessDayBefore(any(LocalDate.class), eq(2)))
                .thenReturn(LocalDate.now().minusDays(2));
        OrderExecution buyEx = buildExec(TransactionType.BUY);
        OrderExecution sellEx = buildExec(TransactionType.SELL);
        when(executionRepository.findUnsettledExecutionsByTimestampBetween(any(), any()))
                .thenReturn(List.of(buyEx, sellEx));

        service.performDailySettlement();

        verify(portfolioService, times(1)).settleBuyTransaction(eq(buyEx));
        verify(portfolioService, times(1)).settleSellTransaction(eq(sellEx));
        verify(executionRepository, times(2)).save(any(OrderExecution.class));
    }

    @Test
    @DisplayName("performDailySettlement: tek bir execution hata verse de diğerleri devam eder")
    void performDailySettlement_continuesOnError() {
        when(businessDayCalculator.getBusinessDayBefore(any(LocalDate.class), eq(2)))
                .thenReturn(LocalDate.now().minusDays(2));
        OrderExecution failing = buildExec(TransactionType.BUY);
        OrderExecution ok = buildExec(TransactionType.SELL);
        when(executionRepository.findUnsettledExecutionsByTimestampBetween(any(), any()))
                .thenReturn(List.of(failing, ok));
        doThrow(new RuntimeException("settle fail")).when(portfolioService).settleBuyTransaction(eq(failing));

        service.performDailySettlement();

        verify(portfolioService, times(1)).settleSellTransaction(eq(ok));
    }

    @Test
    @DisplayName("settleSingleTransaction: BUY -> settleBuyTransaction ve save çağrılır")
    void settleSingleTransaction_buy() {
        OrderExecution ex = buildExec(TransactionType.BUY);
        service.settleSingleTransaction(ex);
        verify(portfolioService, times(1)).settleBuyTransaction(eq(ex));
        verify(executionRepository, times(1)).save(eq(ex));
    }

    @Test
    @DisplayName("settleSingleTransaction: SELL -> settleSellTransaction ve save çağrılır")
    void settleSingleTransaction_sell() {
        OrderExecution ex = buildExec(TransactionType.SELL);
        service.settleSingleTransaction(ex);
        verify(portfolioService, times(1)).settleSellTransaction(eq(ex));
        verify(executionRepository, times(1)).save(eq(ex));
    }

    @Test
    @DisplayName("settleSingleTransaction: Geçersiz type -> SettlementFailedException atar")
    void settleSingleTransaction_invalidType_throws() {
        OrderExecution ex = buildExec(null);
        assertThatThrownBy(() -> service.settleSingleTransaction(ex))
                .isInstanceOf(com.example.finchange.execution.exception.SettlementFailedException.class);
        verify(executionRepository, never()).save(any());
    }
}
