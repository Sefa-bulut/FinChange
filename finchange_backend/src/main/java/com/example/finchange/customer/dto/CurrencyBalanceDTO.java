package com.example.finchange.customer.dto;

import java.math.BigDecimal;

public interface CurrencyBalanceDTO {
    String getCurrency();
    BigDecimal getTotalBalance();
}