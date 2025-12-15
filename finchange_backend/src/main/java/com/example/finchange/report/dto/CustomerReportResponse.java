package com.example.finchange.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReportResponse {

    private CustomerSummaryDTO customerInfo;

    private LocalDateTime reportGeneratedAt;

    private Map<String, BigDecimal> balanceSummaryByCurrency;

    private int totalAccountCount;

    private List<AccountDetailDTO> accounts;
}