package com.example.finchange.customer.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MusteriOnaylari")
public class CustomerConsents extends AuditableBaseEntity {

    @Column(name = "musteri_id", nullable = false)
    private Integer clientId;

    @Column(name = "onaylayan_kullanici_id", nullable = false)
    private Integer grantedByUserId;

    @Column(name = "onay_tipi", nullable = false, length = 100)
    private String consentType;


}