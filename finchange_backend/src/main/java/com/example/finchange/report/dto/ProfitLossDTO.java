package com.example.finchange.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitLossDTO {
    private BigDecimal grossProfit;
    private BigDecimal totalCommission;
    private BigDecimal netProfit;
    private BigDecimal costBasis;
    private List<TradeDetailDTO> trades;
}
