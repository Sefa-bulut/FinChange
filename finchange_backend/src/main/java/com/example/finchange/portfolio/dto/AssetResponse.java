package com.example.finchange.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetResponse {
    private Integer id;
    private String isinCode;
    private String bistCode;
    private String companyName;
    private String sector;
    private String currency;
    private BigDecimal maxOrderValue;
}
