package com.example.finchange.auth.service;

import com.example.finchange.auth.model.Token;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface TokenService {

    Token generate(Claims claims);

    Token generate(Claims claims, String refreshToken);

    void verifyAndValidate(String token);

    Claims getPayload(String token);

    UsernamePasswordAuthenticationToken getAuthentication(String token);

    String extractTokenId(String token);
}
