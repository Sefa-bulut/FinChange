package com.example.finchange.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AccountDetailDTO {
    private String accountNumber;
    private String accountName;
    private String currency;
    private BigDecimal balance;
}
