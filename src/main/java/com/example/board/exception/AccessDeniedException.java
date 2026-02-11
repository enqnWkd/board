package com.example.board.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException {
    private final Errorcode errorcode;

    public AccessDeniedException (Errorcode errorcode) {
        this.errorcode = errorcode;
    }
}
