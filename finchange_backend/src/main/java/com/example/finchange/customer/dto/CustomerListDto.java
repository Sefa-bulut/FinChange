package com.example.finchange.customer.dto;

import com.example.finchange.customer.model.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerListDto {
    private Integer id;
    private String musteriKodu;
    private String gorunenAd; // Gerçek kişi için "Ad Soyad", Tüzel için "Şirket Unvanı"
    private CustomerType customerType;
    private String durum;
    
    // Frontend table için ek alanlar
    private String email;
    private String telefon;
    private String ad;          // Bireysel müşteri için
    private String soyad;       // Bireysel müşteri için  
    private String sirketUnvani; // Kurumsal müşteri için
    private String status;      // durum ile aynı, frontend compatibility için
}