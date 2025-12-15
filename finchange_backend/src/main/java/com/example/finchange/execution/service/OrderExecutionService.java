package com.example.finchange.execution.service;

import com.example.finchange.execution.model.Order;
import java.math.BigDecimal;

public interface OrderExecutionService {
    void executeSingleOrder(Order order, BigDecimal executionPrice);
}
