package com.example.finchange.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountResponse {

    private Integer id;
    private Integer customerId;
    private String accountNumber;
    private String accountName;
    private String currency;
    private BigDecimal balance;
    private BigDecimal blockedBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer lastModifiedBy;

    private Boolean isActive;

    @JsonProperty("availableBalance")
    public BigDecimal getAvailableBalance() {
        if (this.balance == null || this.blockedBalance == null) {
            return BigDecimal.ZERO;
        }
        return this.balance.subtract(this.blockedBalance);
    }
}