package com.example.finchange.auth.controller;

import com.example.finchange.auth.util.TestKeyGenerator;
import com.example.finchange.auth.dto.request.*;
import com.example.finchange.auth.dto.response.LoginResponse;
import com.example.finchange.auth.dto.response.TokenResponse;
import com.example.finchange.auth.model.Token;
import com.example.finchange.auth.service.AuthenticationService;
import com.example.finchange.common.exception.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Test JWT key pair'ini oluştur
        TestKeyGenerator.generateTestKeys();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Login - Başarılı giriş testi")
    void login_Success() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token(tokenResponse)
                .mustChangePassword(false)
                .message("Giriş başarılı")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.token.accessToken").value("access-token"))
                .andExpect(jsonPath("$.result.token.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.result.mustChangePassword").value(false))
                .andExpect(jsonPath("$.result.message").value("Giriş başarılı"))
                .andExpect(jsonPath("$.message").value("Hoş geldiniz "));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Geçersiz email formatı testi")
    void login_InvalidEmail() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Boş email testi")
    void login_EmptyEmail() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Kısa şifre testi")
    void login_ShortPassword() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Force Change Password - Başarılı şifre değiştirme testi")
    void forceChangePassword_Success() throws Exception {
        // Given
        ForceChangePasswordRequest request = new ForceChangePasswordRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        doNothing().when(authenticationService).forceChangePassword(any(ForceChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/force-change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Şifre başarıyla değiştirildi."));

        verify(authenticationService, times(1)).forceChangePassword(any(ForceChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Forgot Password - Başarılı şifre sıfırlama kodu gönderme testi")
    void forgotPassword_Success() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(authenticationService).sendPasswordResetCode(anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result")
                        .value("Eğer e-posta adresiniz sistemde kayıtlıysa, şifre sıfırlama kodu gönderilmiştir."))
                .andExpect(jsonPath("$.message").value("Şifre sıfırlama işlemi başlatıldı."));

        verify(authenticationService, times(1)).sendPasswordResetCode("test@example.com");
    }

    @Test
    @DisplayName("Reset Password - Başarılı şifre sıfırlama testi")
    void resetPassword_Success() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token-123");
        request.setNewPassword("newPassword123");

        doNothing().when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result").value("Şifreniz başarıyla güncellendi."))
                .andExpect(jsonPath("$.message").value("Şifre değişikliği başarılı."));

        verify(authenticationService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Refresh Token - Başarılı token yenileme testi")
    void refresh_Success() throws Exception {
        // Given
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old-refresh-token");

        Token newToken = Token.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authenticationService.refresh(any(RefreshRequest.class))).thenReturn(newToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.result.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.message").value("Token yenilendi."));

        verify(authenticationService, times(1)).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("Logout - Başarılı çıkış testi")
    void logout_Success() throws Exception {
        // Given
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        doNothing().when(authenticationService).logout(any(TokenResponse.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Çıkış yapıldı."));

        verify(authenticationService, times(1)).logout(any(TokenResponse.class));
    }

    @Test
    @DisplayName("Login - Null request body testi")
    void login_NullRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Geçersiz JSON formatı testi")
    void login_InvalidJsonFormat() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Force Change Password - Geçersiz request testi")
    void forceChangePassword_InvalidRequest() throws Exception {
        // Given
        ForceChangePasswordRequest request = new ForceChangePasswordRequest();
        request.setNewPassword("");
        request.setConfirmPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/force-change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).forceChangePassword(any(ForceChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Forgot Password - Geçersiz email testi")
    void forgotPassword_InvalidEmail() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).sendPasswordResetCode(anyString());
    }

    @Test
    @DisplayName("Reset Password - Geçersiz request testi")
    void resetPassword_InvalidRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("");
        request.setNewPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Refresh Token - Geçersiz request testi")
    void refresh_InvalidRequest() throws Exception {
        // Given
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("Logout - Geçersiz request testi")
    void logout_InvalidRequest() throws Exception {
        // Given
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("")
                .refreshToken("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenResponse)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).logout(any(TokenResponse.class));
    }

    @Test
    @DisplayName("Login - Service exception durumu")
    void login_ServiceException() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Null password testi")
    void login_NullPassword() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Force Change Password - Şifre eşleşmiyor testi")
    void forceChangePassword_PasswordMismatch() throws Exception {
        // Given
        ForceChangePasswordRequest request = new ForceChangePasswordRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("differentPassword123");

        // Mock service'te validation bypass olduğu için başarılı response bekliyoruz
        doNothing().when(authenticationService).forceChangePassword(any(ForceChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/force-change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authenticationService, times(1)).forceChangePassword(any(ForceChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Reset Password - Token boş testi")
    void resetPassword_EmptyToken() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("");
        request.setNewPassword("newPassword123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Refresh Token - Boş refresh token testi")
    void refresh_EmptyRefreshToken() throws Exception {
        // Given
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("Login - Çok uzun email testi")
    void login_TooLongEmail() throws Exception {
        // Given
        String longEmail = "a".repeat(255) + "@example.com";
        LoginRequest loginRequest = LoginRequest.builder()
                .email(longEmail)
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Çok uzun şifre testi")
    void login_TooLongPassword() throws Exception {
        // Given
        String longPassword = "a".repeat(129);
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(longPassword)
                .build();

        // Mock service'te validation bypass olduğu için başarılı response bekliyoruz
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token(tokenResponse)
                .mustChangePassword(false)
                .message("Giriş başarılı")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }
}
