package com.example.finchange.portfolio.repository;

import com.example.finchange.portfolio.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Integer> {
    Optional<Asset> findByBistCode(String bistCode);
    Optional<Asset> findByIsinCode(String isinCode);

    boolean existsByBistCode(String bistCode);
    boolean existsByIsinCode(String isinCode);
}
