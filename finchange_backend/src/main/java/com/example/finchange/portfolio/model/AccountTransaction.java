package com.example.finchange.portfolio.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "HesapHareketleri")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransaction extends AuditableBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private CustomerAccount account;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "direction", nullable = false)
    private short direction; // YATIRMA İÇİN 1, ÇEKİM İÇİN -1

    @Column(name = "balance_after_transaction", nullable = false, precision = 18, scale = 4)
    private BigDecimal balanceAfterTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_order_id")
    private Order relatedOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_execution_id")
    private OrderExecution relatedExecution;

    @Column(name = "description", length = 255)
    private String description;
}