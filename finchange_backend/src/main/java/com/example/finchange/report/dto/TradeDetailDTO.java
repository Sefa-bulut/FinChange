package com.example.finchange.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDetailDTO {
    private LocalDateTime buyTime;
    private BigDecimal buyPrice;
    private LocalDateTime sellTime;
    private BigDecimal sellPrice;
    private int lot;
    private BigDecimal profit;
    private BigDecimal commission;
}
