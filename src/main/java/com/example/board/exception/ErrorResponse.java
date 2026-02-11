package com.example.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String error;
    private String message;

    public static ErrorResponse from(Errorcode errorCode) {
        return new ErrorResponse(
                errorCode.getError(),
                errorCode.getMessage()
        );
    }
}
