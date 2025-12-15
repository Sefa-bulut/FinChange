package com.example.finchange.auth.service.impl;


import com.example.finchange.auth.config.TokenConfiguration;
import com.example.finchange.auth.exception.TokenNotValidException;
import com.example.finchange.auth.model.Token;
import com.example.finchange.user.model.Permission;
import com.example.finchange.user.model.Role;
import com.example.finchange.auth.model.enums.TokenClaims;
import com.example.finchange.auth.service.TokenService;
import com.example.finchange.common.util.RandomUtil;
import com.example.finchange.common.util.validation.ListUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenConfiguration tokenConfiguration;

    @Override
    public Token generate(Claims claims) {

        final long currentTimeMillis = System.currentTimeMillis();

        final JwtBuilder tokenBuilder = this.initializeTokenBuilder(currentTimeMillis);

        final Date accessTokenExpiresAt = DateUtils.addMinutes(
                new Date(currentTimeMillis), tokenConfiguration.getTokenExpiration()
        );
        final String accessToken = tokenBuilder
                .id(RandomUtil.generateUUID())
                .expiration(accessTokenExpiresAt)
                .claims(claims)
                .compact();

        final Date refreshTokenExpiresAt = DateUtils.addDays(
                new Date(currentTimeMillis), tokenConfiguration.getRefreshExpiration()
        );

        final String refreshToken = tokenBuilder
                .id(RandomUtil.generateUUID())
                .expiration(refreshTokenExpiresAt)
                .claims(claims)
                .compact();

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String extractTokenId(String token) {
        Claims claims = getPayload(token);
        return claims.getId(); // "jti"
    }



    @Override
    public Token generate(Claims claims, String refreshToken) {

        final long currentTimeMillis = System.currentTimeMillis();

        final JwtBuilder tokenBuilder = this.initializeTokenBuilder(currentTimeMillis);

        final Date accessTokenExpiresAt = DateUtils.addMinutes(
                new Date(currentTimeMillis), tokenConfiguration.getTokenExpiration()
        );
        final String accessToken = tokenBuilder
                .id(RandomUtil.generateUUID())
                .expiration(accessTokenExpiresAt)
                .claims(claims)
                .compact();

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private JwtBuilder initializeTokenBuilder(long currentTimeMillis) {
        return Jwts.builder()
                .header()
                .type(OAuth2AccessToken.TokenType.BEARER.getValue())
                .and()
                .issuer(tokenConfiguration.getIssuer())
                .issuedAt(new Date(currentTimeMillis))
                .signWith(tokenConfiguration.getPrivateKey());
    }



    @Override
    public void verifyAndValidate(String token) {
        try {
            final Jws<Claims> claims = Jwts.parser()
                    .verifyWith(tokenConfiguration.getPublicKey()) //
                    .build()
                    .parseSignedClaims(token);

            final JwsHeader header = claims.getHeader();
            if (!OAuth2AccessToken.TokenType.BEARER.getValue().equals(header.getType())) {
                throw new RequiredTypeException(token);
            }

            if (!Jwts.SIG.RS256.getId().equals(header.getAlgorithm())) {
                throw new io.jsonwebtoken.security.SignatureException(token);
            }

        } catch (MalformedJwtException | ExpiredJwtException | SignatureException | RequiredTypeException exception) {
            throw new TokenNotValidException();
        }
    }


    @Override
    public Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(tokenConfiguration.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }






    @Override
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {

        // 1. JWT'yi doğrula ve parse et
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(tokenConfiguration.getPublicKey())
                .build()
                .parseSignedClaims(token);

        JwsHeader header = claimsJws.getHeader();
        Claims payload = claimsJws.getPayload();

        List<String> roleNames = ListUtil.to(payload.get(TokenClaims.USER_ROLES.getValue()), String.class);
        if (roleNames == null) {
            roleNames = List.of();
        }

        List<String> permissions = ListUtil.to(payload.get(TokenClaims.USER_PERMISSIONS.getValue()), String.class);
        if (permissions == null) {
            permissions = List.of();
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));
        roleNames.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

        Jwt jwt = new Jwt(
                token,
                payload.getIssuedAt().toInstant(),
                payload.getExpiration().toInstant(),
                Map.of(
                        TokenClaims.TYPE.getValue(), header.getType(),
                        TokenClaims.ALGORITHM.getValue(), header.getAlgorithm()
                ),
                payload
        );

        // 6. Authentication nesnesini döndür
        return UsernamePasswordAuthenticationToken.authenticated(jwt, null, authorities);
    }

}
