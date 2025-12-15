package com.example.finchange.report.service;

import com.example.finchange.customer.model.CustomerType;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.service.CustomerService;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.execution.model.enums.TransactionType; // ENUM import
import com.example.finchange.report.dto.*;
import com.example.finchange.report.repository.ReportCustomerAssetReadRepository;
import com.example.finchange.report.repository.SuitabilityProfilesReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.finchange.customer.model.CustomerAccount;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CustomerService clientService;
    private final OrderExecutionRepository orderExecutionRepository;
    private final ReportCustomerAssetReadRepository customerAssetRepo;
    private final SuitabilityProfilesReadRepository suitabilityReadRepo;

    // --- DIŞARIYA AÇIK TEK METOD ---
    public FullReportResponse generateFullReport(String customerCode, LocalDateTime start, LocalDateTime end) {
        // 1) Müşteri ve temel bilgiler
        Customers customer = clientService.getCustomerByCustomerCode(customerCode);
        Integer customerId = customer.getId();
        CustomerSummaryDTO customerInfo = createCustomerSummary(customer);
        List<AccountDetailDTO> accountDetails = createAccountDetails(customerId);

        // 2) Finansal hesaplamalar
        ProfitLossDTO profitLoss = calculateProfitLoss(customerId, start, end);
        KpiDTO kpis = computeKpis(customer, profitLoss);

        // 3) Liste verileri
        List<OpenPositionRow> openPositions = getOpenPositions(customerId);
        List<TradeHistoryRow> tradeHistory = getTradeHistory(customerId, start, end);

        // 4) Paketle ve dön
        return FullReportResponse.builder()
                .customerInfo(customerInfo)
                .reportGeneratedAt(LocalDateTime.now())
                .accounts(accountDetails)
                .kpis(kpis)
                .profitLoss(profitLoss)
                .openPositions(openPositions)
                .tradeHistory(tradeHistory)
                .build();
    }

    // Geçici uyumluluk: Eski imzayı kullanan sınıflar için delegasyon
    public ProfitLossDTO calculateProfitLoss(String customerCode, LocalDateTime start, LocalDateTime end) {
        Customers c = clientService.getCustomerByCustomerCode(customerCode);
        return calculateProfitLoss(c.getId(), start, end);
    }

    // --- PRIVATE HELPER METODLAR ---
    private CustomerSummaryDTO createCustomerSummary(Customers customer) {
        String displayName = CustomerType.TUZEL.equals(customer.getCustomerType())
                ? customer.getCompanyTitle()
                : customer.getName() + " " + customer.getLastName();
        return CustomerSummaryDTO.builder()
                .customerName(displayName)
                .customerType(customer.getCustomerType())
                .customerCode(customer.getCustomerCode())
                .build();
    }

    private List<AccountDetailDTO> createAccountDetails(Integer customerId) {
        List<CustomerAccount> accounts = clientService.getCustomerAccounts(customerId);
        return accounts.stream().map(account -> AccountDetailDTO.builder()
                        .accountNumber(account.getAccountNumber())
                        .accountName(account.getAccountName())
                        .currency(account.getCurrency())
                        .balance(account.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    private ProfitLossDTO calculateProfitLoss(Integer customerId, LocalDateTime start, LocalDateTime end) {
        List<OrderExecution> executions = orderExecutionRepository.findByCustomerAndDateRange(customerId, start, end);

        Queue<OrderExecution> buyQueue = new LinkedList<>();
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO; // kapatılan pozisyonların maliyeti
        List<TradeDetailDTO> tradeDetails = new ArrayList<>();

        for (OrderExecution exec : executions) {
            TransactionType type = exec.getOrder().getTransactionType();

            if (type == TransactionType.BUY) {
                buyQueue.add(cloneExecution(exec));
            } else if (type == TransactionType.SELL) {
                int sellLot = Optional.ofNullable(exec.getExecutedLotAmount()).orElse(0);
                BigDecimal sellPrice = Optional.ofNullable(exec.getExecutedPrice()).orElse(BigDecimal.ZERO);
                LocalDateTime sellTime = exec.getExecutionTimestamp();
                BigDecimal commission = Optional.ofNullable(exec.getCommissionAmount()).orElse(BigDecimal.ZERO);

                while (sellLot > 0 && !buyQueue.isEmpty()) {
                    OrderExecution buy = buyQueue.peek();
                    int buyLot = Optional.ofNullable(buy.getExecutedLotAmount()).orElse(0);
                    BigDecimal buyPrice = Optional.ofNullable(buy.getExecutedPrice()).orElse(BigDecimal.ZERO);
                    LocalDateTime buyTime = buy.getExecutionTimestamp();

                    int matchedLot = Math.min(sellLot, buyLot);
                    BigDecimal profitPerLot = sellPrice.subtract(buyPrice);
                    BigDecimal profit = profitPerLot.multiply(BigDecimal.valueOf(matchedLot));

                    totalProfit = totalProfit.add(profit);
                    totalCommission = totalCommission.add(commission);
                    totalCostBasis = totalCostBasis.add(buyPrice.multiply(BigDecimal.valueOf(matchedLot)));

                    tradeDetails.add(
                            TradeDetailDTO.builder()
                                    .buyTime(buyTime)
                                    .buyPrice(buyPrice)
                                    .sellTime(sellTime)
                                    .sellPrice(sellPrice)
                                    .lot(matchedLot)
                                    .profit(profit)
                                    .commission(commission)
                                    .build()
                    );

                    sellLot -= matchedLot;
                    buy.setExecutedLotAmount(buyLot - matchedLot);
                    if (buy.getExecutedLotAmount() == 0) {
                        buyQueue.remove();
                    }
                }
            }
        }

        BigDecimal netProfit = totalProfit.subtract(totalCommission);

        return ProfitLossDTO.builder()
                .grossProfit(totalProfit)
                .totalCommission(totalCommission)
                .netProfit(netProfit)
                .costBasis(totalCostBasis)
                .trades(tradeDetails)
                .build();
    }

    // FIFO için: execution kopyası
    private OrderExecution cloneExecution(OrderExecution exec) {
        BigDecimal price = Optional.ofNullable(exec.getExecutedPrice()).orElse(BigDecimal.ZERO);
        int lot = Optional.ofNullable(exec.getExecutedLotAmount()).orElse(0);

        // Getter yok → direkt hesapla
        BigDecimal amount = price.multiply(BigDecimal.valueOf(lot));

        return new OrderExecution(
                exec.getId(),
                exec.getOrder(),
                lot,
                price,
                amount, // hesaplanan executedAmount
                exec.getExecutionTimestamp(),
                exec.getSettlementDate(),
                exec.isSettled(),
                Optional.ofNullable(exec.getCommissionAmount()).orElse(BigDecimal.ZERO)
        );
    }

    private KpiDTO computeKpis(Customers customer, ProfitLossDTO profitLoss) {
        Double periodReturnPct = null;
        if (profitLoss != null && profitLoss.getCostBasis() != null
                && profitLoss.getCostBasis().compareTo(BigDecimal.ZERO) > 0) {
            periodReturnPct = profitLoss.getNetProfit()
                    .divide(profitLoss.getCostBasis(), 6, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
        }
        String risk = suitabilityReadRepo.findLastActiveByCustomerId(customer.getId())
                .map(com.example.finchange.customer.model.SuitabilityProfiles::getRiskToleransi)
                .orElse("—");
        return KpiDTO.builder()
                .periodReturnPct(periodReturnPct)
                .benchmarkPct(null)
                .benchmarkCode("BIST100")
                .riskGroup(risk)
                .build();
    }

    private List<OpenPositionRow> getOpenPositions(Integer customerId) {
        return Optional.ofNullable(customerAssetRepo.findOpenPositionsByCustomerId(customerId))
                .orElse(List.of())
                .stream()
                .map(pos -> {
                    var asset = pos.getAsset();
                    String symbol = (asset != null && asset.getBistCode() != null) ? asset.getBistCode() : "—";
                    int lot = pos.getTotalLot();
                    BigDecimal avgCost = pos.getAverageCost() != null ? pos.getAverageCost() : BigDecimal.ZERO;
                    LocalDateTime dt = pos.getCreatedAt() != null ? pos.getCreatedAt() : LocalDateTime.now();
                    BigDecimal amount = avgCost.multiply(BigDecimal.valueOf(lot));
                    return OpenPositionRow.builder()
                            .datetime(dt)
                            .market("BIST Pay Piyasası")
                            .side("B")
                            .price(avgCost)
                            .symbol(symbol)
                            .lot(lot)
                            .avgCost(avgCost)
                            .amount(amount)
                            .pnl(BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TradeHistoryRow> getTradeHistory(Integer customerId, LocalDateTime start, LocalDateTime end) {
        List<OrderExecution> executions = Optional.ofNullable(orderExecutionRepository.findExecutionsWithDetails(customerId, start, end))
                .orElse(List.of());
        return executions.stream().map(exec -> {
            var order = exec.getOrder();
            var asset = order != null ? order.getAsset() : null;
            String symbol = (asset != null && asset.getBistCode() != null) ? asset.getBistCode() : "—";
            String side;
            if (order != null && order.getTransactionType() != null) {
                String name = order.getTransactionType().name();
                if ("BUY".equalsIgnoreCase(name)) side = "B"; else if ("SELL".equalsIgnoreCase(name)) side = "S"; else side = name;
            } else side = "—";
            BigDecimal executedPrice = exec.getExecutedPrice() != null ? exec.getExecutedPrice() : BigDecimal.ZERO;
            int lot = Optional.ofNullable(exec.getExecutedLotAmount()).orElse(0);
            BigDecimal amount = executedPrice.multiply(BigDecimal.valueOf(lot));
            BigDecimal commission = exec.getCommissionAmount() != null ? exec.getCommissionAmount() : BigDecimal.ZERO;
            return TradeHistoryRow.builder()
                    .executionTime(exec.getExecutionTimestamp())
                    .market("BIST Pay Piyasası")
                    .side(side)
                    .symbol(symbol)
                    .lot(lot)
                    .price(executedPrice)
                    .amount(amount)
                    .commission(commission)
                    .build();
        }).collect(Collectors.toList());
    }

}
