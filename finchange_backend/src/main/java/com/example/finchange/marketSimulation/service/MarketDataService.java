package com.example.finchange.marketSimulation.service;

import java.util.List;
import java.util.Map;

import com.example.finchange.marketSimulation.dto.AssetDetailResponse;
import com.example.finchange.marketSimulation.dto.MarketDataResponse;

public interface MarketDataService {


    MarketDataResponse getActiveAssetsWithInitialPrices();

    AssetDetailResponse getAssetDetails(String bistCode);
    
}
