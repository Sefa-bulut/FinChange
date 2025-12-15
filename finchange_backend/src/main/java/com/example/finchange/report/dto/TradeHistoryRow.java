package com.example.finchange.report.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class TradeHistoryRow {
    LocalDateTime executionTime;
    String market;
    String side;
    String symbol;
    int lot;
    BigDecimal price;
    BigDecimal amount;
    BigDecimal commission;
}
