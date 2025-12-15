package com.example.finchange.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserInfoRequestDto {
    
    @NotBlank(message = "Ad boş olamaz.")
    @Size(min = 2, message = "Ad en az 2 karakter olmalıdır.")
    @Pattern(regexp = "^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$", message = "Ad sadece harf içerebilir.")
    private String firstName;
    
    @NotBlank(message = "Soyad boş olamaz.")
    @Size(min = 2, message = "Soyad en az 2 karakter olmalıdır.")
    @Pattern(regexp = "^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$", message = "Soyad sadece harf içerebilir.")
    private String lastName;
    
    @NotBlank(message = "Personel kodu boş olamaz.")
    private String personnelCode;
}