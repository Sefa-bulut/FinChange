package com.example.finchange.portfoliogroup.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class GroupMemberResponse {
    private Integer customerId;
    private String customerCode;
    private String fullName;
    private List<CustomerAccountInfo> accounts;
    
    @Data
    @Builder
    public static class CustomerAccountInfo {
        private Integer id;
        private String accountNumber;
        private String accountName;
        private String currency;
        private BigDecimal balance;
        private BigDecimal blockedBalance;
        private BigDecimal availableBalance;
        private List<CustomerAssetInfo> assets;
    }

    @Data
    @Builder
    public static class CustomerAssetInfo {
        private String bistCode;
        private int totalLot;
        private int blockedLot;
        private int availableLots;
    }
}