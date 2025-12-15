package com.example.finchange.customer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerUpdateRequest {
    
    // İletişim Bilgileri
    private String telefon;
    private String email;
    private String adres;

    // Bireysel Müşteri Alanları
    private String ad;
    private String soyad;
    private String tcKimlikNo; 
    private LocalDate dogumTarihi;

    // Kurumsal Müşteri Alanları
    private String sirketUnvani;
    private String vergiKimlikNo; 
    private String mersisNo;     
    private String yetkiliKisiAdSoyad;
    private String status;
    
    private SuitabilityProfileDto suitabilityProfile;
}