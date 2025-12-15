package com.example.finchange.execution.dto.event;

import java.time.Instant;

public record OrderCancelledEvent(
    Integer orderId,
    Integer customerId,
    Integer assetId,
    String bistCode, 
    String transactionType, 
    String newStatus, 
    int cancelledLots, 
    Instant eventTimestamp
) {}