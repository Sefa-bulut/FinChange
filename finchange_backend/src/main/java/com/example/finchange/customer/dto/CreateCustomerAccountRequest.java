package com.example.finchange.customer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerAccountRequest {

    @NotBlank(message = "Hesap adı boş olamaz.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Hesap adı sadece harf ve rakam içermelidir.")
    private String accountName;

    @NotBlank(message = "Döviz türü belirtilmelidir.")
    @Pattern(regexp = "^(TRY|USD|EUR)$", message = "Döviz türü yalnızca TRY, USD veya EUR olabilir.")
    private String currency;

    @NotNull(message = "İlk bakiye boş olamaz.")
    @DecimalMin(value = "0.0", inclusive = true, message = "İlk bakiye negatif olamaz.")
    private BigDecimal initialBalance ;
}