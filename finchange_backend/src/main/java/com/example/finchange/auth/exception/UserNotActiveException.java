package com.example.finchange.auth.exception;

import com.example.finchange.common.exception.AbstractAuthException;

import java.io.Serial;

public final class UserNotActiveException extends AbstractAuthException {

  @Serial
  private static final long serialVersionUID = 458293847529834752L;

  public UserNotActiveException() {
    super("Hesap aktif değil. Lütfen sistem yöneticinize başvurun.");
  }
}