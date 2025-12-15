package com.example.finchange.report.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class FullReportResponse {
    CustomerSummaryDTO customerInfo;
    LocalDateTime reportGeneratedAt;
    List<AccountDetailDTO> accounts;
    KpiDTO kpis;
    ProfitLossDTO profitLoss;
    List<OpenPositionRow> openPositions;
    List<TradeHistoryRow> tradeHistory;
}
