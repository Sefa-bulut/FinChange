package com.example.finchange.report.dto;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Value
@Builder
public class OpenPositionRow {
    LocalDateTime datetime;
    String market;
    String side;
    BigDecimal price;
    String symbol;
    int lot;
    BigDecimal avgCost;
    BigDecimal amount;
    BigDecimal pnl;
}

