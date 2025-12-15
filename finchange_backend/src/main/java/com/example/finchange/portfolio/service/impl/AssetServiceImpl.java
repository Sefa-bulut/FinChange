package com.example.finchange.portfolio.service.impl;

import com.example.finchange.marketSimulation.service.impl.AssetMatchingServiceImpl;
import com.example.finchange.portfolio.Mapper.AssetMapper;
import com.example.finchange.portfolio.dto.AssetCreateRequest;
import com.example.finchange.portfolio.dto.AssetResponse;
import com.example.finchange.portfolio.exception.AssetAlreadyExistsException;
import com.example.finchange.portfolio.exception.AssetNotFoundException;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    private final AssetMatchingServiceImpl assetMatchingService;

    @Override
    @Cacheable("assets")
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        log.info("Fetching all assets from DB...");
        List<Asset> assets = assetRepository.findAll();
        return assetMapper.toAssetResponseList(assets);
    }

    @Override
    @Cacheable(value = "asset", key = "#bistCode")
    @Transactional(readOnly = true)
    public AssetResponse getAssetByBistCode(String bistCode) {
        log.info("Fetching asset with BIST code: {} from DB...", bistCode);
        Asset asset = assetRepository.findByBistCode(bistCode)
                .orElseThrow(() -> new AssetNotFoundException("Bu BIST koduna sahip varlık bulunamadı: " + bistCode));
        return assetMapper.toAssetResponse(asset);
    }

    @Override
    @Transactional
    @CacheEvict(value = "assets", allEntries = true)
    public AssetResponse createAsset(AssetCreateRequest request) {
        if (assetRepository.existsByBistCode(request.getBistCode())) {
            throw new AssetAlreadyExistsException("Bu BIST kodu zaten mevcut: " + request.getBistCode());
        }
        if (assetRepository.existsByIsinCode(request.getIsinCode())) {
            throw new AssetAlreadyExistsException("Bu ISIN kodu zaten mevcut: " + request.getIsinCode());
        }

        Asset assetToCreate = assetMapper.toEntity(request);
        Asset savedAsset = assetRepository.save(assetToCreate);
        log.info("New asset created with ID: {}. Evicting 'assets' cache.", savedAsset.getId());
        assetMatchingService.findAndWriteSingleMatchToRedis(savedAsset.getBistCode());

        return assetMapper.toAssetResponse(savedAsset);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "assets", allEntries = true),
            @CacheEvict(value = "asset", key = "#result.bistCode")
    })
    public AssetResponse updateAsset(Integer id, AssetCreateRequest request) {
        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException("Güncellenecek varlık bulunamadı. ID: " + id));

        Optional<Asset> assetWithSameBistCode = assetRepository.findByBistCode(request.getBistCode());
        if (assetWithSameBistCode.isPresent() && !Objects.equals(assetWithSameBistCode.get().getId(), existingAsset.getId())) {
            throw new AssetAlreadyExistsException("Bu BIST kodu başka bir varlığa aittir: " + request.getBistCode());
        }

        Optional<Asset> assetWithSameIsinCode = assetRepository.findByIsinCode(request.getIsinCode());
        if (assetWithSameIsinCode.isPresent() && !Objects.equals(assetWithSameIsinCode.get().getId(), existingAsset.getId())) {
            throw new AssetAlreadyExistsException("Bu ISIN kodu başka bir varlığa aittir: " + request.getIsinCode());
        }

        assetMapper.updateAssetFromRequest(request, existingAsset);

        Asset updatedAsset = assetRepository.save(existingAsset);
        log.info("Asset with ID: {} updated. Evicting related caches.", id);
        assetMatchingService.findAndWriteSingleMatchToRedis(updatedAsset.getBistCode());

        return assetMapper.toAssetResponse(updatedAsset);
    }
}