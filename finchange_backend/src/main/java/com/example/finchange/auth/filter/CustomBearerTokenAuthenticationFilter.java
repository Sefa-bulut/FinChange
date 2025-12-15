package com.example.finchange.auth.filter;

import com.example.finchange.auth.model.Token;
import com.example.finchange.auth.service.InvalidTokenService;
import com.example.finchange.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomBearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final InvalidTokenService invalidTokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (Token.isBearerToken(authorizationHeader)) {

            final String jwt = Token.getJwt(authorizationHeader);
            String tokenPreview = jwt != null && jwt.length() > 12 ? jwt.substring(0, 6) + "..." + jwt.substring(jwt.length()-6) : "masked";
            log.debug("[Filter] JWT alındı (masked): {}", tokenPreview);


            log.debug("[Filter] JWT doğrulama başlıyor...");


            tokenService.verifyAndValidate(jwt);

            final String tokenId = tokenService.getPayload(jwt).getId();

            invalidTokenService.checkForInvalidityOfToken(tokenId);

            final var authentication = tokenService.getAuthentication(jwt);

            log.debug("[Filter] Authentication oluşturuldu. Principal türü: {}", authentication.getPrincipal().getClass().getName());
            log.debug("[Filter] Yetkiler sayısı: {}", authentication.getAuthorities() != null ? authentication.getAuthorities().size() : 0);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[Filter] Authentication SecurityContext'e set edildi.");
        } else {
            log.debug("[Filter] Authorization header yok veya Bearer değil.");
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
