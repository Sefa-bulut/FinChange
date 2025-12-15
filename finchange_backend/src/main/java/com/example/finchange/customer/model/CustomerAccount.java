package com.example.finchange.customer.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "MusteriHesaplari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccount extends AuditableBaseEntity {

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customers customer;

    @Column(name = "accountNumber", length = 8, nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "accountName", length = 100, nullable = false)
    private String accountName;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "balance", precision = 18, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "blockedBalance", precision = 18, scale = 4, nullable = false)
    private BigDecimal blockedBalance = BigDecimal.ZERO;

    @Column(name = "isActive", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}