package com.example.finchange.execution.repository;

import com.example.finchange.execution.model.OrderExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderExecutionRepository extends JpaRepository<OrderExecution, Long> {
    @Query("SELECT e FROM OrderExecution e WHERE e.isSettled = false AND e.executionTimestamp >= :startOfDay AND e.executionTimestamp < :endOfDay")
    List<OrderExecution> findUnsettledExecutionsByTimestampBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Override modunda geçmiş dahil tüm netleşmemiş işlemleri almak için
    List<OrderExecution> findByIsSettled(boolean isSettled);

    @Query("""
        select oe from OrderExecution oe
        join fetch oe.order o
        join fetch o.asset a
        join fetch o.customerAccount ca
        where ca.customer.id = :customerId
        and oe.executionTimestamp between :start and :end
    """)
    List<OrderExecution> findExecutionsWithDetails(
            @Param("customerId") Integer customerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
                SELECT oe FROM OrderExecution oe
                JOIN FETCH oe.order o
                JOIN o.customerAccount ca
                JOIN ca.customer c
                WHERE c.id = :customerId
                  AND oe.executionTimestamp BETWEEN :startDate AND :endDate
                ORDER BY oe.executionTimestamp
            """)
    List<OrderExecution> findByCustomerAndDateRange(
            @Param("customerId") int customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}