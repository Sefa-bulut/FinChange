package com.example.finchange.report.repository;

import com.example.finchange.portfolio.model.CustomerAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportCustomerAssetReadRepository extends JpaRepository<CustomerAsset, Long> {

    @Query("""
      select ca from CustomerAsset ca
      join fetch ca.asset a
      where ca.customerId = :customerId and ca.totalLot > 0
    """)
    List<CustomerAsset> findOpenPositionsByCustomerId(Integer customerId);
}
