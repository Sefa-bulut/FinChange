package com.example.finchange.execution.dto;

import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class ValidateLotRequest {

    @NotNull private Integer customerAccountId;
    @NotBlank private String bistCode;
    @NotNull private Integer lotAmount;

    @NotNull private TransactionType transactionType;
    @NotNull private OrderType orderType;
    private BigDecimal limitPrice; 
    
}
