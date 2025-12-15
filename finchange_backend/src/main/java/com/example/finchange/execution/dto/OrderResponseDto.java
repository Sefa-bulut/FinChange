package com.example.finchange.execution.dto;

import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponseDto {
    private Integer id;
    private String orderCode;
    private String batchId;
    private String customerName;
    private String customerCode;
    private String bistCode;
    private TransactionType transactionType;
    private OrderType orderType;
    private OrderStatus status;
    private int initialLotAmount;
    private int filledLotAmount;
    private BigDecimal limitPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}