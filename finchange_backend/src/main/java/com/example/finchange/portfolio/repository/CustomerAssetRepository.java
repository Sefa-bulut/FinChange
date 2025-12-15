package com.example.finchange.portfolio.repository;

import com.example.finchange.portfolio.model.CustomerAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface CustomerAssetRepository extends JpaRepository<CustomerAsset, Integer> {
    Optional<CustomerAsset> findByCustomerIdAndAssetId(Integer customerId, Integer assetId);
    List<CustomerAsset> findByCustomerIdInAndAssetId(List<Integer> customerIds, Integer assetId);
    @EntityGraph(attributePaths = {"asset"}) 
    List<CustomerAsset> findByCustomerIdIn(List<Integer> customerIds);

}