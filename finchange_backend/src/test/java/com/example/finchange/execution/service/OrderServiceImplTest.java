package com.example.finchange.execution.service;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.service.impl.OrderServiceImpl;
import com.example.finchange.portfolio.model.Asset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderServiceImpl - tryToMatchOrdersImmediately tests")
class OrderServiceImplTest {

    @Mock private com.example.finchange.execution.repository.OrderRepository orderRepository;
    @Mock private com.example.finchange.portfolio.service.PortfolioService portfolioService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private com.example.finchange.portfolio.repository.AssetRepository assetRepository;
    @Mock private com.example.finchange.customer.repository.CustomerAccountRepository customerAccountRepository;
    @Mock private com.example.finchange.portfolio.repository.CustomerAssetRepository customerAssetRepository;
    @Mock private com.example.finchange.common.util.SecurityUtils securityUtils;
    @Mock private com.example.finchange.execution.publisher.OrderEventPublisher orderEventPublisher;
    @Mock private com.example.finchange.marketSimulation.service.MarketSessionService marketSessionService;
    @Mock private com.example.finchange.execution.mapper.OrderMapper orderMapper;
    @Mock private com.example.finchange.execution.service.OrderExecutionService orderExecutionService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Mock
    private ValueOperations<String, String> valueOps;

    private Order createOrder(TransactionType txType, OrderType orderType, BigDecimal limitPrice, String bistCode) {
        Asset asset = new Asset();
        asset.setBistCode(bistCode);

        Order order = new Order();
        order.setAsset(asset);
        order.setAssetId(1);
        order.setTransactionType(txType);
        order.setOrderType(orderType);
        order.setLimitPrice(limitPrice);
        order.setInitialLotAmount(10);
        order.setFilledLotAmount(0);
        return order;
    }


    @Test
    @DisplayName("createBulkOrder BUY MARKET: market açıkken başarılı, blokaj ve event yayınlanır")
    void createBulkOrder_buy_market_success() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(50);
        asset.setBistCode("AKBNK");
        asset.setCurrency("TRY");
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(java.util.Optional.of(asset));

        when(valueOps.get("asset:live_price:AKBNK")).thenReturn("10.00");

        var acc1 = new com.example.finchange.customer.model.CustomerAccount(); acc1.setId(1); acc1.setBalance(new BigDecimal("1000")); acc1.setBlockedBalance(BigDecimal.ZERO);
        var acc2 = new com.example.finchange.customer.model.CustomerAccount(); acc2.setId(2); acc2.setBalance(new BigDecimal("2000")); acc2.setBlockedBalance(BigDecimal.ZERO);
        when(customerAccountRepository.findAllById(any())).thenReturn(java.util.List.of(acc1, acc2));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId((int)(Math.random()*10000));
            return o;
        });

        var co1 = new com.example.finchange.execution.dto.CustomerOrderRequest();
        co1.setCustomerAccountId(1); co1.setLotAmount(10);
        var co2 = new com.example.finchange.execution.dto.CustomerOrderRequest();
        co2.setCustomerAccountId(2); co2.setLotAmount(20);
        var req = new com.example.finchange.execution.dto.BulkOrderRequest();
        req.setBistCode("AKBNK");
        req.setOrderType(OrderType.MARKET);
        req.setTransactionType(TransactionType.BUY);
        req.setCustomerOrders(java.util.List.of(co1, co2));

        String batchId = orderServiceImpl.createBulkOrder(req);

        org.assertj.core.api.Assertions.assertThat(batchId).isNotBlank();
        verify(portfolioService, times(2)).blockBalanceForBuyOrder(any(Order.class));

        ArgumentCaptor<com.example.finchange.execution.events.OrdersCreatedEvent> eventCaptor = ArgumentCaptor.forClass(com.example.finchange.execution.events.OrdersCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(eventCaptor.getValue().getCreatedOrderIds()).hasSize(2);
    }

    @Test
    @DisplayName("createBulkOrder MARKET: piyasa kapalıysa hata")
    void createBulkOrder_marketClosed_throws() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(false);
        var asset = new com.example.finchange.portfolio.model.Asset(); asset.setId(60); asset.setBistCode("THYAO");
        when(assetRepository.findByBistCode("THYAO")).thenReturn(java.util.Optional.of(asset));

        var co = new com.example.finchange.execution.dto.CustomerOrderRequest(); co.setCustomerAccountId(1); co.setLotAmount(1);
        var req = new com.example.finchange.execution.dto.BulkOrderRequest();
        req.setBistCode("THYAO"); req.setOrderType(OrderType.MARKET); req.setTransactionType(TransactionType.BUY);
        req.setCustomerOrders(java.util.List.of(co));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderServiceImpl.createBulkOrder(req))
                .isInstanceOf(com.example.finchange.execution.exception.MarketClosedException.class);
    }

    @Test
    @DisplayName("createBulkOrder LIMIT: maxOrderValue aşıldıysa hata")
    void createBulkOrder_maxOrderValueExceeded_throws() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(70); asset.setBistCode("AKBNK"); asset.setCurrency("TRY");
        asset.setMaxOrderValue(new BigDecimal("100")); // düşük sınır
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(java.util.Optional.of(asset));

        var co = new com.example.finchange.execution.dto.CustomerOrderRequest(); co.setCustomerAccountId(1); co.setLotAmount(20); // 20 * 10 = 200 > 100
        var req = new com.example.finchange.execution.dto.BulkOrderRequest();
        req.setBistCode("AKBNK"); req.setOrderType(OrderType.LIMIT); req.setTransactionType(TransactionType.BUY);
        req.setLimitPrice(new BigDecimal("10"));
        req.setCustomerOrders(java.util.List.of(co));

        when(customerAccountRepository.findAllById(any())).thenReturn(java.util.List.of());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderServiceImpl.createBulkOrder(req))
                .isInstanceOf(com.example.finchange.execution.exception.MaxOrderValueExceededException.class);
    }

    @Test
    @DisplayName("createBulkOrder SELL LIMIT: piyasa kapalıyken başarılı, emirler QUEUED ve varlık blokajı yapılır")
    void createBulkOrder_sell_limit_marketClosed_successQueued() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(false); // createOrderEntity -> QUEUED
        var asset = new com.example.finchange.portfolio.model.Asset(); asset.setId(80); asset.setBistCode("EREGL");
        when(assetRepository.findByBistCode("EREGL")).thenReturn(java.util.Optional.of(asset));

        var cust = new com.example.finchange.customer.model.Customers(); cust.setId(1000);
        var acc = new com.example.finchange.customer.model.CustomerAccount(); acc.setId(10); acc.setCustomer(cust);
        when(customerAccountRepository.findAllById(any())).thenReturn(java.util.List.of(acc));
        var casset = new com.example.finchange.portfolio.model.CustomerAsset(); casset.setCustomerId(1000); casset.setTotalLot(50); casset.setBlockedLot(5);
        when(customerAssetRepository.findByCustomerIdInAndAssetId(any(), eq(80))).thenReturn(java.util.List.of(casset));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> { Order o = inv.getArgument(0); o.setId(500); return o; });

        var co = new com.example.finchange.execution.dto.CustomerOrderRequest(); co.setCustomerAccountId(10); co.setLotAmount(10);
        var req = new com.example.finchange.execution.dto.BulkOrderRequest();
        req.setBistCode("EREGL"); req.setOrderType(OrderType.LIMIT); req.setTransactionType(TransactionType.SELL);
        req.setLimitPrice(new BigDecimal("30"));
        req.setCustomerOrders(java.util.List.of(co));

        String batch = orderServiceImpl.createBulkOrder(req);
        org.assertj.core.api.Assertions.assertThat(batch).isNotBlank();

        verify(portfolioService, times(1)).blockAssetForSellOrder(any(Order.class));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(orderCaptor.getAllValues().get(0).getStatus())
                .isEqualTo(com.example.finchange.execution.model.enums.OrderStatus.QUEUED);
    }

    @Test
    @DisplayName("createBulkOrder SELL: müşteri varlığı yetersizse hata")
    void createBulkOrder_sell_insufficientAsset_throws() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        var asset = new com.example.finchange.portfolio.model.Asset(); asset.setId(90); asset.setBistCode("BIMAS");
        when(assetRepository.findByBistCode("BIMAS")).thenReturn(java.util.Optional.of(asset));

        var cust = new com.example.finchange.customer.model.Customers(); cust.setId(2000);
        var acc = new com.example.finchange.customer.model.CustomerAccount(); acc.setId(20); acc.setCustomer(cust);
        when(customerAccountRepository.findAllById(any())).thenReturn(java.util.List.of(acc));
        var casset = new com.example.finchange.portfolio.model.CustomerAsset(); casset.setCustomerId(2000); casset.setTotalLot(5); casset.setBlockedLot(3); // usable 2 < 10
        when(customerAssetRepository.findByCustomerIdInAndAssetId(any(), eq(90))).thenReturn(java.util.List.of(casset));

        when(valueOps.get("asset:live_price:BIMAS")).thenReturn("50.00");

        var co = new com.example.finchange.execution.dto.CustomerOrderRequest(); co.setCustomerAccountId(20); co.setLotAmount(10);
        var req = new com.example.finchange.execution.dto.BulkOrderRequest();
        req.setBistCode("BIMAS"); req.setOrderType(OrderType.MARKET); req.setTransactionType(TransactionType.SELL);
        req.setCustomerOrders(java.util.List.of(co));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderServiceImpl.createBulkOrder(req))
                .isInstanceOf(com.example.finchange.execution.exception.InsufficientAssetException.class);
    }

    @BeforeEach
    void setupRedisMock() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Null veya boş liste verildiğinde hiçbir işlem yapılmaz")
    void tryToMatchOrdersImmediately_nullOrEmpty() {
        orderServiceImpl.tryToMatchOrdersImmediately(null);
        orderServiceImpl.tryToMatchOrdersImmediately(List.of());
        verifyNoInteractions(orderExecutionService);
    }

    @Test
    @DisplayName("Piyasa kapalı ise eşleşme tetiklenmez ve dönüş yapılır")
    void tryToMatchOrdersImmediately_marketClosed() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(false);
        Order o = createOrder(TransactionType.BUY, OrderType.MARKET, null, "AKBNK");
        orderServiceImpl.tryToMatchOrdersImmediately(List.of(o));
        verify(marketSessionService, times(1)).isMarketOpenNow();
        verifyNoInteractions(orderExecutionService);
    }

    @Test
    @DisplayName("Canlı fiyat yoksa eşleşme denenmez")
    void tryToMatchOrdersImmediately_missingLivePrice() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOps.get("asset:live_price:AKBNK")).thenReturn(null);

        Order o = createOrder(TransactionType.BUY, OrderType.MARKET, null, "AKBNK");
        orderServiceImpl.tryToMatchOrdersImmediately(List.of(o));

        verify(orderExecutionService, never()).executeSingleOrder(any(), any());
    }

    @Test
    @DisplayName("Piyasa emri için her durumda executeSingleOrder çağrılır")
    void tryToMatchOrdersImmediately_marketOrder_executes() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOps.get("asset:live_price:AKBNK")).thenReturn("100.50");
        Order o = createOrder(TransactionType.BUY, OrderType.MARKET, null, "AKBNK");

        orderServiceImpl.tryToMatchOrdersImmediately(List.of(o));

        verify(orderExecutionService, times(1)).executeSingleOrder(eq(o), eq(new BigDecimal("100.50")));
    }

    @Test
    @DisplayName("Limit BUY: limitPrice >= currentPrice ise execute edilir, aksi halde edilmez")
    void tryToMatchOrdersImmediately_limitBuy_behavior() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOps.get("asset:live_price:AKBNK")).thenReturn("50.00");

        Order willExecute = createOrder(TransactionType.BUY, OrderType.LIMIT, new BigDecimal("50.00"), "AKBNK");
        Order wontExecute = createOrder(TransactionType.BUY, OrderType.LIMIT, new BigDecimal("49.99"), "AKBNK");

        orderServiceImpl.tryToMatchOrdersImmediately(List.of(willExecute, wontExecute));

        verify(orderExecutionService, times(1)).executeSingleOrder(eq(willExecute), eq(new BigDecimal("50.00")));
        verify(orderExecutionService, never()).executeSingleOrder(eq(wontExecute), any());
    }

    @Test
    @DisplayName("validateLotForCustomer BUY: bakiye yeterliyse valid")
    void validateLot_buy_valid() {
        var req = new com.example.finchange.execution.dto.ValidateLotRequest();
        req.setCustomerAccountId(5);
        req.setBistCode("AKBNK");
        req.setOrderType(OrderType.LIMIT);
        req.setTransactionType(TransactionType.BUY);
        req.setLimitPrice(new BigDecimal("10"));
        req.setLotAmount(3);

        com.example.finchange.customer.model.Customers cust = new com.example.finchange.customer.model.Customers();
        cust.setId(99);
        var acc = new com.example.finchange.customer.model.CustomerAccount();
        acc.setId(5);
        acc.setCustomer(cust);
        acc.setBalance(new BigDecimal("1000"));
        acc.setBlockedBalance(new BigDecimal("0"));
        when(customerAccountRepository.findById(5)).thenReturn(java.util.Optional.of(acc));

        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(10);
        asset.setBistCode("AKBNK");
        asset.setMaxOrderValue(new BigDecimal("10000"));
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(java.util.Optional.of(asset));

        var resp = orderServiceImpl.validateLotForCustomer(req);
        org.assertj.core.api.Assertions.assertThat(resp.isValid()).isTrue();
    }

    @Test
    @DisplayName("validateLotForCustomer BUY: bakiye yetersizse invalid")
    void validateLot_buy_insufficientBalance() {
        var req = new com.example.finchange.execution.dto.ValidateLotRequest();
        req.setCustomerAccountId(6);
        req.setBistCode("AKBNK");
        req.setOrderType(OrderType.LIMIT);
        req.setTransactionType(TransactionType.BUY);
        req.setLimitPrice(new BigDecimal("100"));
        req.setLotAmount(20); 

        com.example.finchange.customer.model.Customers cust = new com.example.finchange.customer.model.Customers();
        cust.setId(100);
        var acc = new com.example.finchange.customer.model.CustomerAccount();
        acc.setId(6);
        acc.setCustomer(cust);
        acc.setBalance(new BigDecimal("1500"));
        acc.setBlockedBalance(new BigDecimal("0"));
        when(customerAccountRepository.findById(6)).thenReturn(java.util.Optional.of(acc));

        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(10);
        asset.setBistCode("AKBNK");
        when(assetRepository.findByBistCode("AKBNK")).thenReturn(java.util.Optional.of(asset));

        var resp = orderServiceImpl.validateLotForCustomer(req);
        org.assertj.core.api.Assertions.assertThat(resp.isValid()).isFalse();
    }

    @Test
    @DisplayName("validateLotForCustomer SELL: lot yeterliyse valid")
    void validateLot_sell_valid() {
        var req = new com.example.finchange.execution.dto.ValidateLotRequest();
        req.setCustomerAccountId(7);
        req.setBistCode("THYAO");
        req.setOrderType(OrderType.MARKET);
        req.setTransactionType(TransactionType.SELL);
        req.setLotAmount(5);

        com.example.finchange.customer.model.Customers cust = new com.example.finchange.customer.model.Customers();
        cust.setId(200);
        var acc = new com.example.finchange.customer.model.CustomerAccount();
        acc.setId(7);
        acc.setCustomer(cust);
        when(customerAccountRepository.findById(7)).thenReturn(java.util.Optional.of(acc));

        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(20);
        asset.setBistCode("THYAO");
        when(assetRepository.findByBistCode("THYAO")).thenReturn(java.util.Optional.of(asset));

        var ca = new com.example.finchange.portfolio.model.CustomerAsset();
        ca.setTotalLot(10);
        ca.setBlockedLot(2);
        when(customerAssetRepository.findByCustomerIdAndAssetId(200, 20)).thenReturn(java.util.Optional.of(ca));

        when(valueOps.get("asset:live_price:THYAO")).thenReturn("50.00");

        var resp = orderServiceImpl.validateLotForCustomer(req);
        org.assertj.core.api.Assertions.assertThat(resp.isValid()).isTrue();
    }

    @Test
    @DisplayName("validateLotForCustomer SELL: lot yetersizse invalid")
    void validateLot_sell_insufficientLots() {
        var req = new com.example.finchange.execution.dto.ValidateLotRequest();
        req.setCustomerAccountId(8);
        req.setBistCode("THYAO");
        req.setOrderType(OrderType.LIMIT);
        req.setTransactionType(TransactionType.SELL);
        req.setLimitPrice(new BigDecimal("10"));
        req.setLotAmount(9);

        com.example.finchange.customer.model.Customers cust = new com.example.finchange.customer.model.Customers();
        cust.setId(201);
        var acc = new com.example.finchange.customer.model.CustomerAccount();
        acc.setId(8);
        acc.setCustomer(cust);
        when(customerAccountRepository.findById(8)).thenReturn(java.util.Optional.of(acc));

        var asset = new com.example.finchange.portfolio.model.Asset();
        asset.setId(21);
        asset.setBistCode("THYAO");
        when(assetRepository.findByBistCode("THYAO")).thenReturn(java.util.Optional.of(asset));

        var ca = new com.example.finchange.portfolio.model.CustomerAsset();
        ca.setTotalLot(10);
        ca.setBlockedLot(5);
        when(customerAssetRepository.findByCustomerIdAndAssetId(201, 21)).thenReturn(java.util.Optional.of(ca));

        var resp = orderServiceImpl.validateLotForCustomer(req);
        org.assertj.core.api.Assertions.assertThat(resp.isValid()).isFalse();
    }

    @Test
    @DisplayName("cancelOrder: sahibi olan kullanıcı iptal edebilir, blokaj çözülür ve event publish edilir")
    void cancelOrder_ownerCanCancel() {
        var auth = new TestingAuthenticationToken("user", "pwd", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityUtils.getCurrentUserId()).thenReturn(123);

        var order = new Order();
        order.setId(55);
        order.setCustomerAccountId(5);
        order.setAssetId(10);
        order.setStatus(com.example.finchange.execution.model.enums.OrderStatus.ACTIVE);
        order.setCreatedBy(123);
        var asset = new com.example.finchange.portfolio.model.Asset(); asset.setId(10); asset.setBistCode("AKBNK");
        var acc = new com.example.finchange.customer.model.CustomerAccount();
        var cust = new com.example.finchange.customer.model.Customers(); cust.setId(99);
        acc.setCustomer(cust);

        when(orderRepository.findById(55)).thenReturn(java.util.Optional.of(order));
        when(customerAccountRepository.findById(5)).thenReturn(java.util.Optional.of(acc));
        when(assetRepository.findById(10)).thenReturn(java.util.Optional.of(asset));

        orderServiceImpl.cancelOrder(55);

        verify(portfolioService, times(1)).releaseBlockForCancelledOrder(eq(order));
        verify(orderEventPublisher, times(1)).publishOrderCancelledEvent(any());
    }

    @Test
    @DisplayName("updateOrder: limit emir geçersiz limitPrice ile hata fırlatır")
    void updateOrder_invalidLimitPrice_throws() {
        var auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityUtils.getCurrentUserId()).thenReturn(999);

        var order = new Order();
        order.setId(77);
        order.setAsset(new com.example.finchange.portfolio.model.Asset());
        order.setStatus(com.example.finchange.execution.model.enums.OrderStatus.ACTIVE);
        when(orderRepository.findById(77)).thenReturn(java.util.Optional.of(order));

        var req = new com.example.finchange.execution.dto.UpdateOrderRequest();
        req.setOrderType(OrderType.LIMIT);
        req.setLotAmount(10);
        req.setLimitPrice(new BigDecimal("0"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderServiceImpl.updateOrder(77, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getOrders: mapper uygulanır ve Page dönülür")
    void getOrders_maps() {
        var pageable = PageRequest.of(0, 10);
        var order = new Order(); order.setId(1);
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<com.example.finchange.execution.model.Order>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(order), pageable, 1));
        when(orderMapper.toOrderResponseDto(eq(order))).thenReturn(org.mockito.Mockito.mock(com.example.finchange.execution.dto.OrderResponseDto.class));

        var page = orderServiceImpl.getOrders(null, null, null, null, null, null, pageable);
        org.assertj.core.api.Assertions.assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Limit SELL: limitPrice <= currentPrice ise execute edilir, aksi halde edilmez")
    void tryToMatchOrdersImmediately_limitSell_behavior() {
        when(marketSessionService.isMarketOpenNow()).thenReturn(true);
        when(valueOps.get("asset:live_price:THYAO")).thenReturn("10.00");

        Order willExecute = createOrder(TransactionType.SELL, OrderType.LIMIT, new BigDecimal("10.00"), "THYAO");
        Order wontExecute = createOrder(TransactionType.SELL, OrderType.LIMIT, new BigDecimal("10.01"), "THYAO");

        orderServiceImpl.tryToMatchOrdersImmediately(List.of(willExecute, wontExecute));

        verify(orderExecutionService, times(1)).executeSingleOrder(eq(willExecute), eq(new BigDecimal("10.00")));
        verify(orderExecutionService, never()).executeSingleOrder(eq(wontExecute), any());
    }
}
