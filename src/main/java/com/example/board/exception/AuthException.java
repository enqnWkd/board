package com.example.board.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AuthException extends AuthenticationException {
    private final Errorcode errorCode;

    public AuthException(Errorcode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}