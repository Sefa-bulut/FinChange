package com.example.finchange.execution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerOrderRequest {

    @NotNull(message = "Müşteri hesap ID'si zorunludur.")
    private Integer customerAccountId;

    @NotNull(message = "Lot miktarı zorunludur.")
    private Integer lotAmount;
}