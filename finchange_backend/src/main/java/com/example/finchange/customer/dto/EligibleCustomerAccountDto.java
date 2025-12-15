package com.example.finchange.customer.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class EligibleCustomerAccountDto {
    private Integer id;
    private String accountName;
    private String currency;
    private BigDecimal balance;
    private BigDecimal blockedBalance;
}