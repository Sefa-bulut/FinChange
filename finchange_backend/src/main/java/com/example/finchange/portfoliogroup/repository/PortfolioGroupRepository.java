package com.example.finchange.portfoliogroup.repository;

import com.example.finchange.portfoliogroup.model.PortfolioGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioGroupRepository extends JpaRepository<PortfolioGroup, Integer> {
    List<PortfolioGroup> findByOwner_Id(Integer ownerId);
    Optional<PortfolioGroup> findByGroupNameAndOwner_Id(String groupName, Integer ownerId);
}