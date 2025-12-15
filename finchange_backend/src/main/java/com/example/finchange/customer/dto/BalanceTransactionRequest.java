package com.example.finchange.customer.dto;

import com.example.finchange.customer.model.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceTransactionRequest {

    @NotNull(message = "İşlem tutarı boş olamaz.")
    private BigDecimal amount;
}