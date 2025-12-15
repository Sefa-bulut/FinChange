package com.example.finchange.marketSimulation.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.marketSimulation.dto.AssetDetailResponse;
import com.example.finchange.marketSimulation.dto.MarketDataResponse;
import com.example.finchange.marketSimulation.service.MarketDataService;
import com.example.finchange.operation.service.SystemDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final SystemDateService systemDateService;

    @GetMapping("/active-assets")
    public ResponseEntity<SuccessResponse<MarketDataResponse>> getActiveAssets() {
        MarketDataResponse activeAssets = marketDataService.getActiveAssetsWithInitialPrices();
        return ResponseEntity
                .ok(SuccessResponse.success(activeAssets));
    }

    @GetMapping("/asset-details/{bistCode}")
    public ResponseEntity<SuccessResponse<AssetDetailResponse>> getAssetDetails(@PathVariable String bistCode) {
        AssetDetailResponse assetDetails = marketDataService.getAssetDetails(bistCode);
        return ResponseEntity
                .ok(SuccessResponse.success(assetDetails));
    }
}