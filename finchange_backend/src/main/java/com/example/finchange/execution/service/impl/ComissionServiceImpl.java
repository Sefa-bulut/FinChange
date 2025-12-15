package com.example.finchange.execution.service.impl;

import com.example.finchange.brokerage.service.BrokerageFirmService;
import com.example.finchange.execution.service.ComissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ComissionServiceImpl implements ComissionService {

    private final BrokerageFirmService brokerageFirmService;

    @Override
    public BigDecimal calculateCommission(BigDecimal totalValue) {
        BigDecimal rate = brokerageFirmService.getActiveCommissionRate();
        return totalValue.multiply(rate);
    }
}