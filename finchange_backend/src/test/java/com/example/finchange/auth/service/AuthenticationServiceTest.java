package com.example.finchange.auth.service;

import com.example.finchange.auth.dto.request.*;
import com.example.finchange.auth.dto.response.LoginResponse;
import com.example.finchange.auth.dto.response.TokenResponse;
import com.example.finchange.auth.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Interface Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
    }

    @Test
    @DisplayName("Login - Mock test")
    void login_MockTest() {
        // Given
        LoginResponse expectedResponse = LoginResponse.builder()
                .token(tokenResponse)
                .mustChangePassword(false)
                .message("Giriş başarılı")
                .build();

        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(expectedResponse);

        // When
        LoginResponse result = authenticationService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken().getAccessToken()).isEqualTo("access-token");
        assertThat(result.getToken().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.isMustChangePassword()).isFalse();
        assertThat(result.getMessage()).contains("başarılı");

        verify(authenticationService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Refresh Token - Mock test")
    void refresh_MockTest() {
        // Given
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        Token expectedToken = Token.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authenticationService.refresh(any(RefreshRequest.class)))
                .thenReturn(expectedToken);

        // When
        Token result = authenticationService.refresh(refreshRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(authenticationService).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("Logout - Mock test")
    void logout_MockTest() {
        // Given
        TokenResponse logoutRequest = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        // When
        authenticationService.logout(logoutRequest);

        // Then
        verify(authenticationService).logout(any(TokenResponse.class));
    }
}
