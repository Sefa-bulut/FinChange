package com.example.finchange.brokerage.repository;

import com.example.finchange.brokerage.model.BrokerageFirm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrokerageFirmRepository extends JpaRepository<BrokerageFirm, Integer> {
    Optional<BrokerageFirm> findByStatus(String status);

    Optional<BrokerageFirm> findByKurumKodu(String kurumKodu);

    boolean existsByStatus(String status);

    boolean existsByStatusAndIdNot(String status, Integer id);
}