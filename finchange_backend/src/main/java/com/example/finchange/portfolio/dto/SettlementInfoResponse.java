package com.example.finchange.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementInfoResponse {
    private Integer customerId;
    private Integer assetId;
    private Integer totalLot;
    private Integer blockedLot;
    private Integer availableLot;
    private LocalDateTime settlementUnlockDateTime;
    private boolean settlementControlsActive;
    private String message;
}
