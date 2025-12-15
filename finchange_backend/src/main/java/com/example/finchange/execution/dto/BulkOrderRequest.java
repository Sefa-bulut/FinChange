package com.example.finchange.execution.dto;

import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BulkOrderRequest {

    @NotBlank(message = "Hisse senedi BIST kodu zorunludur.")
    private String bistCode;

    @NotNull(message = "İşlem tipi zorunludur (BUY/SELL).")
    private TransactionType transactionType;

    @NotNull(message = "Emir tipi zorunludur (LIMIT/MARKET).")
    private OrderType orderType;

    @NotNull(message = "Limit fiyatı zorunludur (Piyasa emri için 0 gönderilebilir).")
    private BigDecimal limitPrice;

    @NotEmpty(message = "En az bir müşteri emri olmalıdır.")
    @Valid
    private List<CustomerOrderRequest> customerOrders;
}