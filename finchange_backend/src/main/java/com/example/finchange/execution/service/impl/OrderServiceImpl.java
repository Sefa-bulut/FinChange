package com.example.finchange.execution.service.impl;


import com.example.finchange.execution.dto.OrderResponseDto;
import com.example.finchange.execution.mapper.OrderMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.execution.dto.BulkOrderRequest;
import com.example.finchange.execution.dto.CustomerOrderRequest;
import com.example.finchange.execution.dto.UpdateOrderRequest;
import com.example.finchange.execution.dto.ValidateLotRequest;
import com.example.finchange.execution.dto.ValidateLotResponse;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.execution.repository.OrderRepository;
import com.example.finchange.execution.service.OrderService;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.repository.CustomerAssetRepository;
import com.example.finchange.portfolio.service.PortfolioService;
import jakarta.persistence.EntityNotFoundException;
import com.example.finchange.execution.exception.MarketClosedException;
import com.example.finchange.execution.exception.InsufficientFundsException;
import com.example.finchange.execution.exception.InsufficientAssetException;
import com.example.finchange.execution.exception.LivePriceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.example.finchange.execution.events.OrdersCreatedEvent;
import com.example.finchange.common.util.SecurityUtils; 
import com.example.finchange.execution.dto.event.OrderCancelledEvent; 
import com.example.finchange.execution.publisher.OrderEventPublisher; 
import java.time.Instant; 
import com.example.finchange.execution.service.OrderExecutionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PortfolioService portfolioService;
    private final StringRedisTemplate redisTemplate;
    private final AssetRepository assetRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerAssetRepository customerAssetRepository;
    private final SecurityUtils securityUtils; 
    private final OrderEventPublisher orderEventPublisher; 
    private final com.example.finchange.marketSimulation.service.MarketSessionService marketSessionService;
    private final OrderMapper orderMapper;
    private final OrderExecutionService orderExecutionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public String createBulkOrder(BulkOrderRequest request) {
        log.info("Toplu {} emri talebi alındı. Hisse: {}", request.getTransactionType(), request.getBistCode());

        Asset asset = assetRepository.findByBistCode(request.getBistCode())
                .orElseThrow(() -> new EntityNotFoundException("Varlık bulunamadı: " + request.getBistCode()));

        if (request.getOrderType() == OrderType.MARKET && !marketSessionService.isMarketOpenNow()) {
            throw new MarketClosedException("Piyasa kapalıyken piyasa emri kabul edilemez.");
        }

        BigDecimal lockedPrice = getLockedPrice(request.getBistCode(), request.getLimitPrice(), request.getOrderType());

        if (request.getOrderType() == OrderType.LIMIT) {
            if (!com.example.finchange.execution.util.PriceValidationUtil.isPriceTickValid(lockedPrice)) {
                throw new IllegalArgumentException("Geçersiz fiyat adımı. Girdiğiniz fiyat, hissenin fiyat aralığına uygun bir adımda olmalıdır.");
            }
        }

        if (asset.getMaxOrderValue() != null && asset.getMaxOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            for (CustomerOrderRequest customerOrder : request.getCustomerOrders()) {
                BigDecimal orderValue = lockedPrice.multiply(new BigDecimal(customerOrder.getLotAmount()));
                if (orderValue.compareTo(asset.getMaxOrderValue()) > 0) {
                    throw new com.example.finchange.execution.exception.MaxOrderValueExceededException(
                            "Emir değeri, bu hisse için belirlenen maksimum değeri (" + asset.getMaxOrderValue() + " " + asset.getCurrency() + ") aşıyor.");
                }
            }
        }

        log.info("Ön kontrol adımı başlatılıyor...");
        if (request.getTransactionType() == TransactionType.BUY) {
            validateBuyOrders(request.getCustomerOrders(), lockedPrice);
        } else {
            validateSellOrders(request.getCustomerOrders(), asset.getId());
        }

        log.info("Asıl işlem döngüsü başlatılıyor...");
        String batchId = UUID.randomUUID().toString();
        List<Order> createdOrders = new ArrayList<>();
        request.getCustomerOrders().forEach(customerOrder -> {
            Order newOrder = createOrderEntity(customerOrder, asset, request, batchId, lockedPrice);
            Order savedOrder = orderRepository.save(newOrder);
            
            if (request.getTransactionType() == TransactionType.BUY) {
                portfolioService.blockBalanceForBuyOrder(savedOrder);
            } else {
                portfolioService.blockAssetForSellOrder(savedOrder);
            }
            createdOrders.add(savedOrder);
        });

        List<Integer> createdOrderIds = createdOrders.stream().map(Order::getId).collect(Collectors.toList());
        eventPublisher.publishEvent(new OrdersCreatedEvent(createdOrderIds));

        log.info("Toplu emir başarıyla oluşturuldu ve {} adet emir ID'si ile olay yayınlandı. Batch ID: {}", createdOrderIds.size(), batchId);
        return batchId;
    }

    private Order createOrderEntity(CustomerOrderRequest customerOrder,
                                    Asset asset,
                                    BulkOrderRequest bulkRequest,
                                    String batchId,
                                    BigDecimal priceToUse) {
        return Order.builder()
                .orderCode(UUID.randomUUID().toString())
                .batchId(batchId)
                .customerAccountId(customerOrder.getCustomerAccountId())
                .assetId(asset.getId())
                .transactionType(bulkRequest.getTransactionType())
                .orderType(bulkRequest.getOrderType())
                .status(marketSessionService.isMarketOpenNow() ? OrderStatus.ACTIVE : OrderStatus.QUEUED)
                .initialLotAmount(customerOrder.getLotAmount())
                .filledLotAmount(0)
                .limitPrice(priceToUse)
                .validityType("GUNLUK")
                .build();
    }

    private void validateBuyOrders(List<CustomerOrderRequest> customerOrders, BigDecimal lockedPrice) {
        List<Integer> accountIds = customerOrders.stream().map(CustomerOrderRequest::getCustomerAccountId).collect(Collectors.toList());
        Map<Integer, CustomerAccount> accountsMap = customerAccountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(CustomerAccount::getId, Function.identity()));

        for (CustomerOrderRequest orderRequest : customerOrders) {
            CustomerAccount account = accountsMap.get(orderRequest.getCustomerAccountId());
            if (account == null) throw new EntityNotFoundException("Müşteri hesabı bulunamadı: " + orderRequest.getCustomerAccountId());

            BigDecimal requiredAmount = lockedPrice.multiply(new BigDecimal(orderRequest.getLotAmount()));
            BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());

            if (availableBalance.compareTo(requiredAmount) < 0) {
                throw new InsufficientFundsException("Hesap ID " + account.getId() + " için yetersiz bakiye.");
            }
        }
        log.info("Tüm {} BUY emri için bakiye kontrolleri başarıyla tamamlandı.", customerOrders.size());
    }

    private void validateSellOrders(List<CustomerOrderRequest> customerOrders, Integer assetId) {
        List<Integer> accountIds = customerOrders.stream().map(CustomerOrderRequest::getCustomerAccountId).collect(Collectors.toList());
        Map<Integer, CustomerAccount> accountsMap = customerAccountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(CustomerAccount::getId, Function.identity()));
        
        List<Integer> customerIds = accountsMap.values().stream().map(ca -> ca.getCustomer().getId()).collect(Collectors.toList());
        Map<Integer, CustomerAsset> assetsMap = customerAssetRepository.findByCustomerIdInAndAssetId(customerIds, assetId).stream()
                .collect(Collectors.toMap(CustomerAsset::getCustomerId, asset -> asset));

        for (CustomerOrderRequest orderRequest : customerOrders) {
            CustomerAccount account = accountsMap.get(orderRequest.getCustomerAccountId());
            if (account == null) throw new EntityNotFoundException("Müşteri hesabı bulunamadı: " + orderRequest.getCustomerAccountId());
            
            CustomerAsset asset = assetsMap.get(account.getCustomer().getId());
            if (asset == null || (asset.getTotalLot() - asset.getBlockedLot()) < orderRequest.getLotAmount()) {
                throw new InsufficientAssetException("Müşteri ID " + account.getCustomer().getId() + " için yetersiz varlık.");
            }
        }
        log.info("Tüm {} SELL emri için varlık kontrolleri başarıyla tamamlandı.", customerOrders.size());
    }

    private BigDecimal getLockedPrice(String bistCode, BigDecimal limitPrice, OrderType orderType) {
        if (orderType == OrderType.LIMIT) {
            return limitPrice;
        }
        String livePriceStr = redisTemplate.opsForValue().get("asset:live_price:" + bistCode);
        if (livePriceStr == null) {
            throw new LivePriceUnavailableException("Piyasa kapalı veya canlı fiyat yok: " + bistCode);
        }
        return new BigDecimal(livePriceStr);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateLotResponse validateLotForCustomer(ValidateLotRequest request) {
        try {
            CustomerAccount account = customerAccountRepository.findById(request.getCustomerAccountId())
                    .orElseThrow(() -> new EntityNotFoundException("Hesap bulunamadı."));

            Asset asset = assetRepository.findByBistCode(request.getBistCode())
                    .orElseThrow(() -> new EntityNotFoundException("Varlık bulunamadı."));

            BigDecimal priceForLimitChecks = null;
            if (request.getTransactionType() == TransactionType.BUY) {
                BigDecimal priceToUse = getLockedPrice(asset.getBistCode(), request.getLimitPrice(), request.getOrderType());
                if (priceToUse == null || priceToUse.compareTo(BigDecimal.ZERO) <= 0) {
                    return ValidateLotResponse.builder().valid(false).message("Geçerli bir fiyat belirlenemedi.").build();
                }

                BigDecimal requiredAmount = priceToUse.multiply(new BigDecimal(request.getLotAmount()));
                BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());

                if (availableBalance.compareTo(requiredAmount) < 0) {
                    return ValidateLotResponse.builder().valid(false).message("Yetersiz bakiye.").build();
                }
                priceForLimitChecks = priceToUse;
            }
            // SELL 
            else if (request.getTransactionType() == TransactionType.SELL) {
                CustomerAsset customerAsset = customerAssetRepository
                        .findByCustomerIdAndAssetId(account.getCustomer().getId(), asset.getId())
                        .orElse(null);

                if (customerAsset == null || (customerAsset.getTotalLot() - customerAsset.getBlockedLot()) < request.getLotAmount()) {
                    return ValidateLotResponse.builder().valid(false).message("Yetersiz lot.").build();
                }
                priceForLimitChecks = getLockedPrice(asset.getBistCode(), request.getLimitPrice(), request.getOrderType());
                if (priceForLimitChecks == null || priceForLimitChecks.compareTo(BigDecimal.ZERO) <= 0) {
                    return ValidateLotResponse.builder().valid(false).message("Geçerli bir fiyat belirlenemedi.").build();
                }
            }

            BigDecimal maxOrderValue = asset.getMaxOrderValue();
            if (maxOrderValue != null && maxOrderValue.compareTo(BigDecimal.ZERO) > 0 && priceForLimitChecks != null) {
                BigDecimal orderValue = priceForLimitChecks.multiply(new BigDecimal(request.getLotAmount()));
                if (orderValue.compareTo(maxOrderValue) > 0) {
                    String msg = String.format("Maksimum emir değeri aşıldı. Maks: %s, Emir: %s", maxOrderValue, orderValue);
                    return ValidateLotResponse.builder().valid(false).message(msg).build();
                }
            }

            return ValidateLotResponse.builder().valid(true).message("Geçerli.").build();
        } catch (Exception e) {
            log.error("Lot validasyonu sırasında hata: {}", e.getMessage());
            return ValidateLotResponse.builder().valid(false).message(e.getMessage()).build();
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("İptal edilecek emir bulunamadı: " + orderId));

        boolean isOwner = order.getCreatedBy() != null && order.getCreatedBy().equals(currentUserId);
        
        boolean isAdmin = auth.getAuthorities().stream()
                              .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Bu emri iptal etme yetkiniz yok.");
        }

        if (order.getStatus() != OrderStatus.QUEUED && order.getStatus() != OrderStatus.ACTIVE && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
            throw new IllegalStateException("Bu emir iptal edilemez. Mevcut durum: " + order.getStatus());
        }

        int remainingLotsForCancel = order.getInitialLotAmount() - order.getFilledLotAmount();
        if (remainingLotsForCancel <= 0) {
            throw new IllegalStateException("Emrin iptal edilecek kısmı bulunmuyor. Emir zaten tamamen gerçekleşmiş olabilir.");
        }

        portfolioService.releaseBlockForCancelledOrder(order);

        order.setStatus(OrderStatus.CANCELLED);

        Integer customerIdForEvent;
        String bistCodeForEvent;

        CustomerAccount accForEvent = customerAccountRepository.findById(order.getCustomerAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Müşteri hesabı bulunamadı: " + order.getCustomerAccountId()));
        customerIdForEvent = accForEvent.getCustomer().getId();

        Asset assetForEvent = assetRepository.findById(order.getAssetId())
                .orElseThrow(() -> new EntityNotFoundException("Varlık bulunamadı: " + order.getAssetId()));
        bistCodeForEvent = assetForEvent.getBistCode();

        OrderCancelledEvent event = new OrderCancelledEvent(
            order.getId(),
            customerIdForEvent,
            order.getAssetId(),
            bistCodeForEvent,
            order.getTransactionType().name(),
            order.getStatus().name(),
            order.getInitialLotAmount() - order.getFilledLotAmount(),
            Instant.now()
        );
        orderEventPublisher.publishOrderCancelledEvent(event);

        log.info("Emir ID {} kullanıcı ID {} tarafından başarıyla iptal edildi.", orderId, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrders(String batchId, Integer customerId, Integer assetId, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (batchId != null && !batchId.isBlank()) {
                predicates.add(cb.equal(root.get("batchId"), batchId));
            }
            if (customerId != null) {
                Join<Order, CustomerAccount> accountJoin = root.join("customerAccount");
                predicates.add(cb.equal(accountJoin.get("customer").get("id"), customerId));
            }
            if (assetId != null) {
                predicates.add(cb.equal(root.get("assetId"), assetId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(status.toUpperCase())));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        return orderPage.map(orderMapper::toOrderResponseDto);
    }

    @Override
    @Transactional
    public void updateOrder(Integer orderId, UpdateOrderRequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Güncellenecek emir bulunamadı: " + orderId));

        boolean isOwner = order.getCreatedBy() != null && order.getCreatedBy().equals(currentUserId);
        boolean isAdmin = auth.getAuthorities().stream()
                              .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Bu emri düzenleme yetkiniz yok.");
        }

        if (order.getStatus() != OrderStatus.ACTIVE && order.getStatus() != OrderStatus.QUEUED) {
            throw new IllegalStateException("Bu emir düzenlenemez. Mevcut durum: " + order.getStatus());
        }

        log.info("Emir ID {} güncelleme işlemi başlatıldı. Eski Lot: {}, Yeni Lot: {}",
                orderId, order.getInitialLotAmount(), request.getLotAmount());

        portfolioService.releaseBlockForCancelledOrder(order);

        order.setOrderType(request.getOrderType());

        BigDecimal priceToUse;
        if (request.getOrderType() == OrderType.LIMIT) {
            if (request.getLimitPrice() == null || request.getLimitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Limit emir için geçerli bir fiyat girilmelidir.");
            }
            if (!com.example.finchange.execution.util.PriceValidationUtil.isPriceTickValid(request.getLimitPrice())) {
                throw new IllegalArgumentException("Geçersiz fiyat adımı.");
            }
            priceToUse = request.getLimitPrice();
        } else {
            priceToUse = getCurrentMarketPrice(order.getAsset().getBistCode());
        }
        order.setLimitPrice(priceToUse);

        int oldLotAmount = order.getInitialLotAmount();
        int newLotAmount = request.getLotAmount();
        order.setInitialLotAmount(newLotAmount);
        
        if (order.getFilledLotAmount() > newLotAmount) {
            throw new IllegalArgumentException("Yeni lot miktarı, gerçekleşmiş lot miktarından (" + order.getFilledLotAmount() + ") küçük olamaz.");
        }

        Asset asset = order.getAsset();
        if (asset.getMaxOrderValue() != null && asset.getMaxOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal orderValue = priceToUse
                    .multiply(new BigDecimal(newLotAmount));
            if (orderValue.compareTo(asset.getMaxOrderValue()) > 0) {
                throw new IllegalArgumentException("Emir değeri, bu hisse için belirlenen maksimum değeri (" + asset.getMaxOrderValue() + " " + asset.getCurrency() + ") aşıyor.");
            }
        }

        if (TransactionType.BUY.equals(order.getTransactionType())) {
            portfolioService.blockBalanceForBuyOrder(order);
        } else {
            portfolioService.blockAssetForSellOrder(order);
        }

        Order savedOrder = orderRepository.save(order);
        
        log.info("Emir ID {} kullanıcı ID {} tarafından başarıyla güncellendi. Eski lot: {}, Yeni lot: {}",
                orderId, currentUserId, oldLotAmount, newLotAmount);

        log.info("Emir ID {} güncellemesi sonrası anında eşleşme kontrolü tetikleniyor.", savedOrder.getId());
        tryToMatchOrdersImmediately(List.of(savedOrder));
    }

    private BigDecimal getCurrentMarketPrice(String bistCode) {
        String livePriceStr = redisTemplate.opsForValue().get("asset:live_price:" + bistCode);
        if (livePriceStr == null) {
            throw new IllegalStateException("Piyasa emri için canlı fiyat bulunamadı: " + bistCode);
        }
        return new BigDecimal(livePriceStr);
    }

    @Override
    @Transactional
    public void tryToMatchOrdersImmediately(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        if (!marketSessionService.isMarketOpenNow()) {
            log.info("Piyasa kapalı. Anında eşleşme denenmeyecek. Emirler kuyruğa alındı.");
            return;
        }

        String bistCode = orders.get(0).getAsset().getBistCode();
        BigDecimal currentPrice;
        try {
            String livePriceStr = redisTemplate.opsForValue().get("asset:live_price:" + bistCode);
            if (livePriceStr == null) {
                log.warn("Anında eşleşme için canlı fiyat bulunamadı: {}", bistCode);
                return;
            }
            currentPrice = new BigDecimal(livePriceStr);
        } catch (Exception e) {
            log.error("Anında eşleşme için canlı fiyat alınırken hata oluştu.", e);
            return;
        }

        log.info("{} için anında eşleşme kontrolü başlatıldı. Mevcut Fiyat: {}", bistCode, currentPrice);

        for (Order order : orders) {
            boolean shouldExecute = false;
            if (order.getOrderType() == OrderType.MARKET) {
                shouldExecute = true;
            } else if (order.getOrderType() == OrderType.LIMIT) {
                if (order.getTransactionType() == TransactionType.BUY && order.getLimitPrice().compareTo(currentPrice) >= 0) {
                    shouldExecute = true;
                } else if (order.getTransactionType() == TransactionType.SELL && order.getLimitPrice().compareTo(currentPrice) <= 0) {
                    shouldExecute = true;
                }
            }

            if (shouldExecute) {
                log.info("ANINDA EŞLEŞME: Emir ID {} gerçekleşiyor.", order.getId());
                orderExecutionService.executeSingleOrder(order, currentPrice);
            }
        }
    }
}