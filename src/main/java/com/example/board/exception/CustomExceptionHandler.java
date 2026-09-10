package com.example.board.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        Errorcode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.from(code));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        Errorcode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.from(code));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        Errorcode code = e.getErrorcode();
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.from(code));
    }

    @ExceptionHandler(ContentInspectionException.class)
    public ResponseEntity<ErrorResponse> handleContentInspectionException(ContentInspectionException e) {
        Errorcode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.from(code));
    }

}
