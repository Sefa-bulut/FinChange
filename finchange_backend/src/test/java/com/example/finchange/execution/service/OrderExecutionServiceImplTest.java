package com.example.finchange.execution.service;

import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.execution.dto.event.OrderExecutedEvent;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.publisher.OrderEventPublisher;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.execution.service.impl.OrderExecutionServiceImpl;
import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.marketSimulation.service.MarketSessionService;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderExecutionServiceImpl - executeSingleOrder tests")
class OrderExecutionServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderExecutionRepository orderExecutionRepository;
    @Mock private ComissionService comissionService;
    @Mock private OrderEventPublisher orderEventPublisher;
    @Mock private BusinessDayCalculator businessDayCalculator;
    @Mock private PortfolioService portfolioService;
    @Mock private MarketSessionService marketSessionService;
    @Mock private CustomerAccountRepository customerAccountRepository;
    @Mock private AssetRepository assetRepository;

    @InjectMocks
    private OrderExecutionServiceImpl service;

    private Order baseOrder;

    @BeforeEach
    void setup() {
        baseOrder = new Order();
        baseOrder.setId(1);
        baseOrder.setAssetId(10);
        baseOrder.setCustomerAccountId(20);
        baseOrder.setInitialLotAmount(10);
        baseOrder.setFilledLotAmount(0);
        baseOrder.setOrderType(OrderType.LIMIT);
        baseOrder.setLimitPrice(new BigDecimal("100"));
        baseOrder.setTransactionType(TransactionType.BUY);
        baseOrder.setStatus(OrderStatus.ACTIVE);

        lenient().when(comissionService.calculateCommission(any())).thenReturn(new BigDecimal("1.23"));
        lenient().when(businessDayCalculator.getBusinessDayAfter(any(LocalDate.class), eq(2))).thenReturn(LocalDate.now().plusDays(2));

        Customers customer = new Customers();
        customer.setId(99);
        customer.setCustomerCode("CUST-99");
        CustomerAccount acc = new CustomerAccount();
        acc.setId(20);
        acc.setCustomer(customer);
        lenient().when(customerAccountRepository.findById(20)).thenReturn(Optional.of(acc));

        Asset asset = new Asset();
        asset.setId(10);
        asset.setBistCode("AKBNK");
        lenient().when(assetRepository.findById(10)).thenReturn(Optional.of(asset));
    }

    @Test
    @DisplayName("remainingLots <= 0 ise işlem yapılmaz")
    void noRemainingLots_returns() {
        baseOrder.setFilledLotAmount(10);
        service.executeSingleOrder(baseOrder, new BigDecimal("99.5"));
        verify(orderExecutionRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderEventPublisher);
    }

    @Test
    @DisplayName("Başarılı akış: execution kaydı oluşturulur, order güncellenir, event yayınlanır")
    void successFlow_createsExecution_updatesOrder_publishesEvent() {
        when(marketSessionService.areSettlementControlsActive()).thenReturn(true);

        service.executeSingleOrder(baseOrder, new BigDecimal("99.50"));

        ArgumentCaptor<OrderExecution> execCaptor = ArgumentCaptor.forClass(OrderExecution.class);
        verify(orderExecutionRepository).save(execCaptor.capture());
        OrderExecution savedExec = execCaptor.getValue();
        assertThat(savedExec.getExecutedPrice()).isEqualByComparingTo("99.50");
        assertThat(savedExec.getCommissionAmount()).isEqualByComparingTo("1.23");
        assertThat(savedExec.getExecutedLotAmount()).isEqualTo(10);
        assertThat(savedExec.getLockedPrice()).isEqualByComparingTo("100"); // from order limit

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getFilledLotAmount()).isEqualTo(10);
        assertThat(savedOrder.getStatus()).isIn(OrderStatus.PARTIALLY_FILLED, OrderStatus.FILLED);

        verify(portfolioService, times(1)).blockAssetForBuyExecution(any(OrderExecution.class));

        verify(orderEventPublisher, times(1)).publishOrderExecutedEvent(any(OrderExecutedEvent.class));
    }

    @Test
    @DisplayName("Hata durumunda emir FAILED yapılır")
    void onException_orderMarkedFailed() {
        when(comissionService.calculateCommission(any())).thenThrow(new RuntimeException("calc error"));

        service.executeSingleOrder(baseOrder, new BigDecimal("90"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("Piyasa emrinde lockedPrice execution price olur")
    void marketOrder_lockedPriceEqualsExecutionPrice() {
        baseOrder.setOrderType(OrderType.MARKET);
        service.executeSingleOrder(baseOrder, new BigDecimal("77.77"));
        ArgumentCaptor<OrderExecution> execCaptor = ArgumentCaptor.forClass(OrderExecution.class);
        verify(orderExecutionRepository).save(execCaptor.capture());
        assertThat(execCaptor.getValue().getLockedPrice()).isEqualByComparingTo("77.77");
    }

    @Test
    @DisplayName("SELL + settlement kontrol kapalı ise anında settleSellTransaction çağrılır")
    void sellImmediateSettlementWhenOverrideOff() {
        baseOrder.setTransactionType(TransactionType.SELL);
        when(marketSessionService.areSettlementControlsActive()).thenReturn(false);

        service.executeSingleOrder(baseOrder, new BigDecimal("50"));
        verify(portfolioService, times(1)).settleSellTransaction(any(OrderExecution.class));
    }
}
