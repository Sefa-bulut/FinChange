package com.example.finchange.auth.model;


import com.example.finchange.common.model.enums.BeanScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;


@Component
@Scope(value = BeanScope.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Identity {


    public String getAccessToken() {
        return this.getJwt().getTokenValue();
    }


    private Jwt getJwt() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
