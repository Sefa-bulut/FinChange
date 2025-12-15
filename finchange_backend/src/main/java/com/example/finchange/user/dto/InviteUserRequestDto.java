package com.example.finchange.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class InviteUserRequestDto {

    @NotBlank(message = "İsim boş olamaz")
    @Size(min = 2, max = 50, message = "İsim 2 ile 50 karakter arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyisim boş olamaz")
    @Size(min = 2, max = 50, message = "Soyisim 2 ile 50 karakter arasında olmalıdır")
    private String lastName;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @NotEmpty(message = "En az bir rol seçilmelidir")
    private List<String> roleNames;

    @NotBlank(message = "Personel kodu boş olamaz")
    @Size(max = 50)
    private String personnelCode;
}