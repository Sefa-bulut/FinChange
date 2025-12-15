package com.example.finchange.execution.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.execution.model.enums.OrderStatus;
import com.example.finchange.execution.model.enums.OrderType;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.portfolio.model.Asset;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "Emirler")
public class Order extends AuditableBaseEntity {

    @Column(name = "order_code", unique = true, nullable = false, length = 36)
    private String orderCode;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    @Column(name = "customer_account_id", nullable = false)
    private int customerAccountId;

    @Column(name = "asset_id", nullable = false)
    private int assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id", insertable = false, updatable = false)
    private CustomerAccount customerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "initial_lot_amount", nullable = false)
    private int initialLotAmount;

    @Column(name = "filled_lot_amount", nullable = false)
    private int filledLotAmount;

    @Column(name = "limit_price", precision = 18, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "validity_type", length = 10)
    private String validityType;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}