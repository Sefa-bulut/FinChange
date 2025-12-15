package com.example.finchange.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForceChangePasswordRequest {
    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 8, max = 255, message = "Şifre en az 8, en fazla 255 karakter olmalıdır")
    private String newPassword;
    
    @NotBlank(message = "Şifre onayı boş olamaz")
    @Size(min = 8, max = 255, message = "Şifre en az 8, en fazla 255 karakter olmalıdır")
    private String confirmPassword;
}