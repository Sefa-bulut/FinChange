package com.example.finchange.marketSimulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class MarketDataResponse {
    private LocalDateTime dates;
    private List<Map<String, Object>> assets;
}