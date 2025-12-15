package com.example.finchange.portfolio.controller;

import com.example.finchange.portfolio.dto.AssetCreateRequest;
import com.example.finchange.portfolio.dto.AssetResponse;
import com.example.finchange.portfolio.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<AssetResponse> createAsset(@RequestBody @Valid AssetCreateRequest request) {
        AssetResponse createdAsset = assetService.createAsset(request);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        List<AssetResponse> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/by-bist-code/{bistCode}")
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<AssetResponse> getAssetByBistCode(@PathVariable String bistCode) {
        AssetResponse asset = assetService.getAssetByBistCode(bistCode);
        return ResponseEntity.ok(asset);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<AssetResponse> updateAsset(@PathVariable Integer id, @RequestBody @Valid AssetCreateRequest request) {
        AssetResponse updatedAsset = assetService.updateAsset(id, request);
        return ResponseEntity.ok(updatedAsset);
    }
}