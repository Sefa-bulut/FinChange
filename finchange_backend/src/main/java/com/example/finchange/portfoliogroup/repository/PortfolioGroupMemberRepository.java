package com.example.finchange.portfoliogroup.repository;

import com.example.finchange.customer.model.Customers;
import com.example.finchange.portfoliogroup.model.PortfolioGroupMember;
import com.example.finchange.portfoliogroup.model.PortfolioGroupMemberId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioGroupMemberRepository extends JpaRepository<PortfolioGroupMember, PortfolioGroupMemberId> {
    
    @Modifying
    void deleteByPortfolioGroup_IdAndCustomer_Id(Integer groupId, Integer customerId);

    @Query("SELECT c FROM PortfolioGroupMember pgm " +
           "JOIN pgm.customer c " +
           "WHERE pgm.portfolioGroup.id = :groupId")
    List<Customers> findActiveMembersByGroupId(Integer groupId);
}