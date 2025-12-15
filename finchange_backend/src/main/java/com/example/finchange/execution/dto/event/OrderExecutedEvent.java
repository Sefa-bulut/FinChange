package com.example.finchange.execution.dto.event;

import java.math.BigDecimal;
import java.time.Instant;


public record OrderExecutedEvent(
        Integer orderId,
        Integer customerId,
        String customerCode,
        Integer assetId,
        String bistCode,
        String transactionType,

        String newStatus,

        int executedLots,
        BigDecimal executedPrice,
        BigDecimal commissionAmount,
        int remainingLots,

        Instant eventTimestamp
) {}