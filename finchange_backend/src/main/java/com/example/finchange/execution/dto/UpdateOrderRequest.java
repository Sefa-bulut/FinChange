package com.example.finchange.execution.dto;

import com.example.finchange.execution.model.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateOrderRequest {

    @NotNull(message = "Emir tipi zorunludur (LIMIT/MARKET).")
    private OrderType orderType;

    @DecimalMin(value = "0.01", message = "Limit fiyat 0.01'den büyük olmalıdır.")
    private BigDecimal limitPrice;

    @NotNull(message = "Lot miktarı zorunludur.")
    @Min(value = 1, message = "Lot miktarı en az 1 olmalıdır.")
    private Integer lotAmount;
}