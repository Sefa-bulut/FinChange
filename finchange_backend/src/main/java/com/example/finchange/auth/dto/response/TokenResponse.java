package com.example.finchange.auth.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponse {

    @NotBlank(message = "Access token boş olamaz")
    private String accessToken;

    @NotBlank(message = "Refresh token boş olamaz")
    private String refreshToken;

}
