package com.example.finchange.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetCreateRequest {

    @NotBlank(message = "ISIN Kodu boş olamaz.")
    @Size(max = 12, message = "ISIN Kodu en fazla 12 karakter olabilir.")
    private String isinCode;

    @NotBlank(message = "BIST Kodu boş olamaz.")
    @Size(max = 10, message = "BIST Kodu en fazla 10 karakter olabilir.")
    private String bistCode;

    @NotBlank(message = "Şirket Adı boş olamaz.")
    private String companyName;

    private String sector;

    @NotBlank(message = "Para Birimi boş olamaz.")
    @Size(max = 3, message = "Para Birimi 3 karakter olmalıdır.")
    private String currency;

    private Integer settlementDays;

    private BigDecimal maxOrderValue;
}