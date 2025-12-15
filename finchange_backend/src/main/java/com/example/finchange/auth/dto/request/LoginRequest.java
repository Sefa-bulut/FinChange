package com.example.finchange.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    @Size(max = 255, message = "Email en fazla 255 karakter olabilir")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, max = 255, message = "Şifre en az 8, en fazla 255 karakter olmalıdır")
    private String password;
}
