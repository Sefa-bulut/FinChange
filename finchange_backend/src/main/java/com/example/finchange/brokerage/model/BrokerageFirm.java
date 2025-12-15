package com.example.finchange.brokerage.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "AraciKurumlar")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageFirm extends AuditableBaseEntity {

    @Column(name = "kurum_kodu", nullable = false, unique = true, length = 20)
    private String kurumKodu;

    @Column(name = "kurum_adi", nullable = false, length = 255)
    private String kurumAdi;

    @Column(name = "api_url", length = 512)
    private String apiUrl;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "integration_type", length = 50)
    private String integrationType;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "commission_rate", nullable = false, precision = 7, scale = 6)
    private BigDecimal commissionRate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}