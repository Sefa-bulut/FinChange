package com.example.finchange.portfolio.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import com.example.finchange.customer.model.Customers;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "MusteriVarliklari")
public class CustomerAsset extends AuditableBaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "asset_id", nullable = false)
    private Integer assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customers customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    private Asset asset;

    @Column(name = "total_lot")
    private int totalLot;

    @Column(name = "blocked_lot")
    private int blockedLot;

    @Column(name = "average_cost", precision = 18, scale = 4)
    private BigDecimal averageCost;

    @Version
    @Column(name = "version")
    private Long version;

    @Transient
    public int getAvailableLots() {
        return this.totalLot - this.blockedLot;
    }
}