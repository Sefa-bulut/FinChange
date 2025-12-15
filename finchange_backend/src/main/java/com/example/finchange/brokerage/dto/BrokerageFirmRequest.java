package com.example.finchange.brokerage.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerageFirmRequest {

    @NotBlank(message = "Kurum kodu zorunludur")
    @Size(max = 20, message = "Kurum kodu en fazla 20 karakter olabilir")
    private String kurumKodu;

    @NotBlank(message = "Kurum adı zorunludur")
    @Size(max = 255, message = "Kurum adı en fazla 255 karakter olabilir")
    private String kurumAdi;

    @Size(max = 512, message = "API URL en fazla 512 karakter olabilir")
    private String apiUrl;

    @Size(max = 100, message = "Kullanıcı adı en fazla 100 karakter olabilir")
    private String username;

    @Size(max = 255, message = "Şifre en fazla 255 karakter olabilir")
    private String password;

    @Size(max = 50, message = "Entegrasyon tipi en fazla 50 karakter olabilir")
    private String integrationType;

    @Email(message = "Geçerli bir e-posta giriniz")
    @Size(max = 255, message = "E-posta en fazla 255 karakter olabilir")
    private String email;

    @NotNull(message = "Komisyon oranı (yüzde) zorunludur")
    @DecimalMin(value = "0", inclusive = true, message = "Komisyon oranı en az 0 olmalıdır")
    @DecimalMax(value = "100", inclusive = true, message = "Komisyon oranı en fazla 100 olmalıdır")
    private BigDecimal commissionRatePercent;

    @NotBlank(message = "Durum zorunludur")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Durum 'ACTIVE' veya 'INACTIVE' olmalıdır")
    private String status;
}
