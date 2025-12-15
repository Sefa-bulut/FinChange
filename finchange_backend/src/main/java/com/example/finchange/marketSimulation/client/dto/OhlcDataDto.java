package com.example.finchange.marketSimulation.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OhlcDataDto {

    @JsonProperty("close_price")
    private Double closePrice;

    @JsonProperty("high_price")
    private Double highPrice;

    @JsonProperty("low_price")
    private Double lowPrice;

    @JsonProperty("open_price")
    private Double openPrice;

    @JsonProperty("asset_code")
    private String assetCode;

    @JsonProperty("data_date")
    private String dataDate;
}