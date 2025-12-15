package com.example.finchange.auth.controller;

import com.example.finchange.auth.dto.request.*;
import com.example.finchange.auth.dto.response.LoginResponse;
import com.example.finchange.auth.dto.response.TokenResponse;
import com.example.finchange.auth.model.Token;
import com.example.finchange.auth.service.AuthenticationService;
import com.example.finchange.common.model.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        return ResponseEntity.ok(
                SuccessResponse.success(loginResponse, "Hoş geldiniz " )
        );
    }

    @PostMapping("/force-change-password")
    public ResponseEntity<SuccessResponse<Void>> forceChangePassword(@RequestBody @Valid ForceChangePasswordRequest request) {
        authenticationService.forceChangePassword(request);
        return ResponseEntity.ok(
                SuccessResponse.success(null, "Şifre başarıyla değiştirildi.")
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authenticationService.sendPasswordResetCode(request.getEmail());
        return ResponseEntity.ok(
                SuccessResponse.success("Eğer e-posta adresiniz sistemde kayıtlıysa, şifre sıfırlama kodu gönderilmiştir.", "Şifre sıfırlama işlemi başlatıldı.")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(
                SuccessResponse.success("Şifreniz başarıyla güncellendi.", "Şifre değişikliği başarılı.")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<Token>> refresh(@RequestBody @Valid RefreshRequest request) {
        Token newTokens = authenticationService.refresh(request);
        return ResponseEntity.ok(
                SuccessResponse.success(newTokens, "Token yenilendi.")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@RequestBody @Valid TokenResponse tokenResponse) {
        authenticationService.logout(tokenResponse);
        return ResponseEntity.ok(
                SuccessResponse.success(null, "Çıkış yapıldı.")
        );
    }

}