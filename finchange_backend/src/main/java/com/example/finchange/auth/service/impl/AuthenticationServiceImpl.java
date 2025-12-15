package com.example.finchange.auth.service.impl;

import com.example.finchange.auth.dto.request.RefreshRequest;
import com.example.finchange.auth.exception.PasswordMismatchException;
import com.example.finchange.auth.exception.UserNotActiveException;
import com.example.finchange.auth.exception.UserPasswordNotValidException;
import com.example.finchange.auth.model.Token;
import com.example.finchange.auth.dto.request.ForceChangePasswordRequest;
import com.example.finchange.auth.dto.request.LoginRequest;
import com.example.finchange.auth.dto.response.LoginResponse;
import com.example.finchange.auth.dto.response.TokenResponse;
import com.example.finchange.auth.repository.PasswordResetTokenRepository;
import com.example.finchange.auth.service.InvalidTokenService;
import com.example.finchange.notification.service.EmailSenderService;
import com.example.finchange.user.model.Permission;
import com.example.finchange.user.model.Role;
import com.example.finchange.auth.model.enums.TokenClaims;
import com.example.finchange.auth.service.AuthenticationService;
import com.example.finchange.auth.service.TokenService;
import com.example.finchange.user.exception.UserNotFoundException;
import com.example.finchange.user.model.User;
import com.example.finchange.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.example.finchange.auth.exception.AccountLockedException;
import com.example.finchange.auth.dto.request.ResetPasswordRequest;
import com.example.finchange.auth.model.entity.PasswordResetToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailSenderService emailSenderService;
    private final InvalidTokenService invalidTokenService;

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 30;


    @Override
    public void logout(TokenResponse tokenResponse) {
        List<String> tokenIds = new ArrayList<>();

        // Access token ve Refresh token'ı al
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        // Refresh token kontrolü
        tokenService.verifyAndValidate(refreshToken);
        tokenIds.add(tokenService.extractTokenId(refreshToken));
        Claims refreshClaims = tokenService.getPayload(refreshToken);

        // Access token kontrolü
        tokenService.verifyAndValidate(accessToken);
        tokenIds.add(tokenService.extractTokenId(accessToken));
        Claims accessClaims = tokenService.getPayload(accessToken);

        // En uzun kalan süresini al (her ikisinin expire süresine göre TTL belirlenebilir)
        Date latestExpiry = refreshClaims.getExpiration().after(accessClaims.getExpiration())
                ? refreshClaims.getExpiration()
                : accessClaims.getExpiration();

        // Token'ları geçersiz kıl
        invalidTokenService.invalidateTokens(new HashSet<>(tokenIds), latestExpiry);
    }



    @Override
    public Token refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        // 1. Refresh token'ı doğrula (imza, expiration vs.)
        tokenService.verifyAndValidate(refreshToken);

        // 2. Token daha önce logout edilmiş mi kontrol et
        String tokenId = tokenService.extractTokenId(refreshToken);
        invalidTokenService.checkForInvalidityOfToken(tokenId);

        // 3. Token içinden kullanıcı bilgilerini al
        Claims claims = tokenService.getPayload(refreshToken);
        String username = claims.getSubject();

        // 4. Kullanıcıyı veritabanından bul
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 5. Yeni token üret
        return tokenService.generate(claims, refreshToken);
    }


    @Override
    public LoginResponse login(LoginRequest request) {
        String loginAttemptKey = "login:attempts:" + request.getEmail();
        String lockedKey = "login:locked:" + request.getEmail();

        // 1. Adım: Hesap kilitli mi diye Redis'i kontrol et.
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockedKey))) {
            throw new AccountLockedException();
        }

        // 2. Adım: Kullanıcıyı veritabanında bul.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + request.getEmail()));

        // 3. Adım: Kullanıcı aktif mi diye kontrol et.
        if (!user.isActive()) {
            throw new UserNotActiveException();
        }

        // 4. Adım: Şifre doğru mu diye kontrol et.
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Şifre yanlışsa, hatalı giriş sayacını yönet.
            handleFailedLoginAttempt(loginAttemptKey, lockedKey);
            throw new UserPasswordNotValidException();
        }

        // 5. Adım: Giriş başarılıysa, hatalı giriş sayacını Redis'ten sil.
        redisTemplate.delete(loginAttemptKey);

        // 6. Adım: Geri kalan orijinal login mantığı ile devam et.
        boolean mustChangePassword = user.isMustChangePassword();
        final Claims claims = generateClaims(user);
        Token token = tokenService.generate(claims);
        String message = mustChangePassword ? "Yeni şifre almak zorundasınız." : "Giriş başarılı.";

        return LoginResponse.builder()
                .token(TokenResponse.builder()
                        .accessToken(token.getAccessToken())
                        .refreshToken(token.getRefreshToken())
                        .build())
                .mustChangePassword(mustChangePassword)
                .message(message)
                .build();
    }

    private Claims generateClaims(User user) {
        final ClaimsBuilder claimsBuilder = Jwts.claims();

        claimsBuilder.add(TokenClaims.USER_ID.getValue(), String.valueOf(user.getId()));
        claimsBuilder.add(TokenClaims.USER_FIRST_NAME.getValue(), user.getFirstName());
        claimsBuilder.add(TokenClaims.USER_LAST_NAME.getValue(), user.getLastName());
        claimsBuilder.add(TokenClaims.USER_MAIL.getValue(), user.getEmail());
        claimsBuilder.add(TokenClaims.USER_STATUS.getValue(), user.isActive());


        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        claimsBuilder.add(TokenClaims.USER_ROLES.getValue(), roleNames);


        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toList());
        claimsBuilder.add(TokenClaims.USER_PERMISSIONS.getValue(), permissions);

        return claimsBuilder.build();
    }

    @Override
    public void forceChangePassword(ForceChangePasswordRequest request) {
        String email = extractEmailFromJwt();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + email));

        validatePasswords(request.getNewPassword(), request.getConfirmPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }


    @Override
    @Transactional
    public void sendPasswordResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı."));

        String token = RandomStringUtils.randomNumeric(6);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(expiryDate);
        tokenRepository.save(resetToken);

        emailSenderService.sendPasswordResetEmail(user.getEmail(), token);
    }


    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Geçersiz şifre sıfırlama kodu."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Şifre sıfırlama kodunun süresi dolmuş.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Şifresini sıfırlayan kullanıcı artık değiştirmek zorunda değil.
        userRepository.save(user);

        tokenRepository.delete(resetToken); // Token'ı kullandıktan sonra sil.
    }


    private String extractEmailFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Kimlik doğrulama bilgisi geçersiz veya JWT değil.");
        }

        String email = jwt.getClaim("userMail");
        if (email == null) {
            throw new IllegalStateException("JWT içinde 'userMail' claim'i bulunamadı.");
        }

        return email;
    }

    private void validatePasswords(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Şifreler uyuşmuyor.");
        }
    }

    private void handleFailedLoginAttempt(String attemptKey, String lockedKey) {

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);


        if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockedKey, "true", LOCK_TIME_DURATION, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
        }
    }

}