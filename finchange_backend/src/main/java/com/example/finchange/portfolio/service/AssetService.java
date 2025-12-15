package com.example.finchange.portfolio.service;

import com.example.finchange.portfolio.dto.AssetCreateRequest;
import com.example.finchange.portfolio.dto.AssetResponse;

import java.util.List;

public interface AssetService {
    List<AssetResponse> getAllAssets();
    AssetResponse getAssetByBistCode(String bistCode);
    AssetResponse createAsset(AssetCreateRequest request);
    AssetResponse updateAsset(Integer id, AssetCreateRequest request);
}