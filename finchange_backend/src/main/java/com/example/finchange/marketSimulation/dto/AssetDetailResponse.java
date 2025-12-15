package com.example.finchange.marketSimulation.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AssetDetailResponse {
    private String bistCode;
    private String companyName;
    private String currency;
    private BigDecimal previousClose;
    private BigDecimal dailyHigh;
    private BigDecimal dailyLow;
    private BigDecimal openPrice;
    private BigDecimal livePrice; 
}