package com.example.finchange.execution.service;
import java.math.BigDecimal;
public interface ComissionService {
    BigDecimal calculateCommission(BigDecimal totalValue);
}