package com.example.finchange.customer.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "Musteriler")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Customers extends AuditableBaseEntity {

    @Column(name = "musteri_kodu", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(name = "kaydi_acan_kullanici_id", nullable = false)
    private Integer registeredUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "musteri_tipi", nullable = false, length = 50)
    private CustomerType customerType;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "telefon", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "ikametgah_adresi", nullable = false)
    private String residenceAddress;

    // Gerçek Kişi Alanları
    @Column(name = "ad", length = 50)
    private String name;

    @Column(name = "soyad", length = 50)
    private String lastName;

    @Column(name = "tc_kimlik_no", unique = true, length = 11)
    private String idNumber;

    @Column(name = "dogum_tarihi")
    private LocalDate dateOfBirth;

    @Column(name = "uyruk", length = 50)
    private String nationality;

    // Tüzel Kişi Alanları
    @Column(name = "sirket_unvani", length = 255)
    private String CompanyTitle;

    @Column(name = "vergi_kimlik_no", unique = true, length = 10)
    private String taxIDNumber;

    @Column(name = "mersis_no", unique = true, length = 16)
    private String mersisNo;

    @Column(name = "merkez_adresi")
    private String centerAddress;

    @Column(name = "yetkili_kisi_ad_soyad", length = 100)
    private String authorizedPersonNameSurname;

    // Belge yolları MusteriBelgeleri tablosunda ayrı tutuluyor
}
