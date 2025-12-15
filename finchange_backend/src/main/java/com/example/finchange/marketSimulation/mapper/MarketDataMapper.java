package com.example.finchange.marketSimulation.mapper;

import java.util.Map;

public class MarketDataMapper {

    public static Map<String, Object> mapOhlcDataToAssetMap(String bistCode, Map<Object, Object> ohlcData) {
        return Map.<String, Object>of(
                "bistCode", bistCode,
                "openPrice", Double.parseDouble((String) ohlcData.getOrDefault("openPrice", "0")),
                "highPrice", Double.parseDouble((String) ohlcData.getOrDefault("dailyHigh", "0")),
                "lowPrice", Double.parseDouble((String) ohlcData.getOrDefault("dailyLow", "0")),
                "closePrice", Double.parseDouble((String) ohlcData.getOrDefault("previousClose", "0"))
        );
    }
}