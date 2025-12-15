package com.example.finchange.customer.dto;

import com.example.finchange.customer.model.CustomerType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerDetailDto {
    private Integer id;
    private String musteriKodu;
    private CustomerType customerType;
    private String durum;

    // İletişim Bilgileri (Ortak)
    private String telefon;
    private String email;
    private String adres;

    // Bireysel Müşteri Bilgileri
    private String gorunenAd; // "Ad Soyad"
    private String ad;
    private String soyad;
    private String tckn;
    private LocalDate dogumTarihi;

    // Kurumsal Müşteri Bilgileri
    private String sirketUnvani; // gorunenAd yerine bunu da kullanabiliriz
    private String vergiNo;
    private String mersisNo;
    private String yetkiliKisiAdSoyad;

    // Yerindelik Profili
    private SuitabilityProfileDto suitabilityProfile;
}
