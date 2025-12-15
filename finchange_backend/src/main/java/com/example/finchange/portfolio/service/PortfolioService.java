package com.example.finchange.portfolio.service;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;

import java.math.BigDecimal;

public interface PortfolioService {
    void deposit(Integer accountId, BigDecimal amount, String description);
    void withdraw(Integer accountId, BigDecimal amount, String description);
    void blockBalanceForBuyOrder(Order order);
    void releaseBlockForCancelledOrder(Order order);
    void blockAssetForSellOrder(Order order);
    void blockAssetForBuyExecution(OrderExecution orderExecution);
    void increaseHoldingsImmediately(OrderExecution orderExecution);
    void applySellExecutionHold(OrderExecution orderExecution);
    void releaseAllBlocksForOverride();
    void settleBuyTransaction(OrderExecution orderExecution);
    void settleSellTransaction(OrderExecution orderExecution);
}
