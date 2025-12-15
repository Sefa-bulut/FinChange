package com.example.finchange.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Güvenlik bağlamından (Security Context) o anki kimliği doğrulanmış kullanıcının ID'sini alır.
     * @return Kullanıcı ID'si.
     * @throws IllegalStateException Kullanıcı kimliği JWT'den alınamazsa veya kullanıcı login olmamışsa.
     */
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("Kullanıcı kimliği JWT'den alınamadı veya geçerli bir oturum yok.");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userIdClaim = jwt.getClaimAsString("userId");

        if (userIdClaim == null) {
            throw new IllegalStateException("JWT içinde 'userId' claim'i bulunamadı.");
        }

        try {
            return Integer.parseInt(userIdClaim);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("'userId' claim'i geçerli bir sayıya dönüştürülemedi.", e);
        }
    }
}