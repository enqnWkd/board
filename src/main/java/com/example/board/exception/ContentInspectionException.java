package com.example.board.exception;

import lombok.Getter;

@Getter
public class ContentInspectionException extends RuntimeException {

    private final Errorcode errorCode;

    public ContentInspectionException(Errorcode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
