package com.example.finchange.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private TokenResponse token;
    private boolean mustChangePassword;
    private String message;
}
