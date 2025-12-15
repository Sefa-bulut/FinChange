package com.example.finchange.customer.dto;

import com.example.finchange.customer.model.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerCreateRequest {

    @NotBlank(message = "Müşteri kodu boş olamaz.")
    @Size(max = 50)
    private String musteriKodu;

    @NotNull(message = "Müşteri tipi boş olamaz.")
    private CustomerType customerType;

    @NotBlank(message = "Telefon numarası boş olamaz.")
    @Pattern(regexp = "^\\(?\\d{3}\\)?[-.\s]?\\d{3}[-.\s]?\\d{2}[-.\s]?\\d{2}$", message = "Geçersiz telefon formatı.")
    private String telefon;

    @NotBlank(message = "E-posta adresi boş olamaz.")
    @Email(message = "Geçersiz e-posta formatı.")
    private String email;

    @NotBlank(message = "İkametgah/Merkez adresi boş olamaz.")
    private String adres;

    // Bireysel müşteri alanları
    @Size(max = 50)
    private String ad;

    @Size(max = 50)
    private String soyad;

    @Pattern(regexp = "^\\d{11}$", message = "T.C. Kimlik Numarası 11 haneli rakamlardan oluşmalıdır.")
    private String tcKimlikNo;

    private LocalDate dogumTarihi;

    // Kurumsal müşteri alanları
    @Size(max = 255)
    private String sirketUnvani;

    @Pattern(regexp = "^\\d{10}$", message = "Vergi Kimlik Numarası 10 haneli rakamlardan oluşmalıdır.")
    private String vergiKimlikNo;

    @Pattern(regexp = "^\\d{16}$", message = "Mersis Numarası 16 haneli rakamlardan oluşmalıdır.")
    private String mersisNo;

    @Size(max = 100)
    private String yetkiliKisiAdSoyad;

    @NotNull(message = "Yerindelik profili bilgileri zorunludur.")
    @Valid
    private SuitabilityProfileDto suitabilityProfile;
}