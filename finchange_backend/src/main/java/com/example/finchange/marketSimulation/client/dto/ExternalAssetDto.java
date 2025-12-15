package com.example.finchange.marketSimulation.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// Sadece ihtiyacımız olan alanları tanımlıyoruz.
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalAssetDto {

    @JsonProperty("code")
    private String code;

    @JsonProperty("issuer_name")
    private String companyName;

    @JsonProperty("security_type")
    private String securityType;

    @JsonProperty("status")
    private String status;

    @JsonProperty("isin")
    private String isinCode;

    @JsonProperty("currency")
    private String currency;
}