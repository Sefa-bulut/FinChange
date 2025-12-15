package com.example.finchange.auth.exception;

import com.example.finchange.common.exception.AbstractAuthException;

import java.io.Serial;

public final class AccountLockedException extends AbstractAuthException {

    @Serial
    private static final long serialVersionUID = 847293847529834752L;

    public AccountLockedException() {
        super("Çok fazla hatalı giriş.");
    }
}
