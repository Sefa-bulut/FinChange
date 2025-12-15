package com.example.finchange.portfolio.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Varliklar")
public class Asset extends AuditableBaseEntity {

    @Column(name = "isin_code", unique = true, nullable = false, length = 12)
    private String isinCode;

    @Column(name = "bist_code", unique = true, nullable = false, length = 10)
    private String bistCode;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "settlement_days")
    private Integer settlementDays;

    @Column(name = "max_order_value", precision = 18, scale = 2)
    private BigDecimal maxOrderValue;
}