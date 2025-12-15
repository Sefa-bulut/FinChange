package com.example.finchange.brokerage.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerageFirmResponse {
    private Integer id;
    private String kurumKodu;
    private String kurumAdi;
    private String apiUrl;
    private String username;
    private String integrationType;
    private String email;
    private BigDecimal commissionRatePercent;
    private String status;
}
