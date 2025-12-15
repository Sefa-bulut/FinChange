package com.example.finchange.marketSimulation.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateEvent {
    private String assetCode;
    private BigDecimal price;
    private Instant timestamp;
}