package com.example.finchange.auth.service;

import com.example.finchange.auth.dto.request.ForceChangePasswordRequest;
import com.example.finchange.auth.dto.request.LoginRequest;
import com.example.finchange.auth.dto.request.RefreshRequest;
import com.example.finchange.auth.dto.request.ResetPasswordRequest;
import com.example.finchange.auth.dto.response.LoginResponse;
import com.example.finchange.auth.dto.response.TokenResponse;
import com.example.finchange.auth.model.Token;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
    void forceChangePassword(ForceChangePasswordRequest request);
    Token refresh(RefreshRequest refreshRequest);
    void sendPasswordResetCode(String email);
    void resetPassword(ResetPasswordRequest request);
    void logout(TokenResponse tokenResponse);
}
