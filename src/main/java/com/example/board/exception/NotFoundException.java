package com.example.board.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final Errorcode errorCode;

    public NotFoundException(Errorcode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
