package com.example.finchange.portfolio.repository;

import com.example.finchange.portfolio.model.AccountTransaction;
import com.example.finchange.execution.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM AccountTransaction t WHERE t.transactionType = :type AND t.relatedOrder = :order")
    java.math.BigDecimal sumAmountByTypeAndOrder(@Param("type") String type, @Param("order") Order order);
}