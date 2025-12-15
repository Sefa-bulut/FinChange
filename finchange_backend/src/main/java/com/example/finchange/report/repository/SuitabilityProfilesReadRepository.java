package com.example.finchange.report.repository;

import com.example.finchange.customer.model.SuitabilityProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SuitabilityProfilesReadRepository extends JpaRepository<SuitabilityProfiles, Integer> {

    @Query(value = """
        SELECT TOP 1 *
        FROM YerindelikProfilleri
        WHERE musteri_id = :customerId AND is_active = 1
        ORDER BY id DESC
        """, nativeQuery = true)
    Optional<SuitabilityProfiles> findLastActiveByCustomerId(@Param("customerId") Integer customerId);
}
