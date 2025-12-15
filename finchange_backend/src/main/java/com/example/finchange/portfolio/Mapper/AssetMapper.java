package com.example.finchange.portfolio.Mapper;

import com.example.finchange.portfolio.dto.AssetCreateRequest;
import com.example.finchange.portfolio.dto.AssetResponse;
import com.example.finchange.portfolio.model.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetResponse toAssetResponse(Asset asset);
    List<AssetResponse> toAssetResponseList(List<Asset> assets);
    Asset toEntity(AssetCreateRequest request);

    void updateAssetFromRequest(AssetCreateRequest request, @MappingTarget Asset asset);
}