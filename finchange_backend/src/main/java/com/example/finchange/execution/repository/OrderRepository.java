package com.example.finchange.execution.repository;

import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.enums.OrderStatus; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>,JpaSpecificationExecutor<Order> { 
    @Query("SELECT o FROM Order o WHERE o.assetId = :assetId AND o.status = 'ACTIVE' AND o.transactionType = 'BUY' AND o.limitPrice >= :newPrice ORDER BY o.limitPrice DESC, o.createdAt ASC")
    List<Order> findMatchingBuyOrders(Integer assetId, BigDecimal newPrice);

    @Query("SELECT o FROM Order o WHERE o.assetId = :assetId AND o.status = 'ACTIVE' AND o.transactionType = 'SELL' AND o.limitPrice <= :newPrice ORDER BY o.limitPrice ASC, o.createdAt ASC")
    List<Order> findMatchingSellOrders(Integer assetId, BigDecimal newPrice);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Override
    @EntityGraph(attributePaths = {"customerAccount", "asset"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.asset WHERE o.id IN :ids")
    List<Order> findByIdInWithAsset(@Param("ids") List<Integer> ids);
}