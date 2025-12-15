package com.example.finchange.auth.exception;

import com.example.finchange.common.exception.AbstractServerException;

public class KeyPairGenerationException extends AbstractServerException {
  public KeyPairGenerationException(Throwable cause) {
    super("error occurred while generating key pair", cause);
  }
}
