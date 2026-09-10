package com.example.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Errorcode {

    UNAUTHORIZED(401, "UNAUTHORIZED", "인증이 필요합니다."),
    EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(403, "ACCESS_DENIED", "접근 권한이 없습니다."),

    ADVERTISEMENT_CONTENT(400, "ADVERTISEMENT_CONTENT", "광고성 게시글은 등록할 수 없습니다."),
    INAPPROPRIATE_CONTENT(400, "INAPPROPRIATE_CONTENT", "부적절한 내용이 포함되어 있습니다."),
    OPENAI_SERVICE_UNAVAILABLE(503, "OPENAI_SERVICE_UNAVAILABLE", "콘텐츠 검사 서비스를 일시적으로 사용할 수 없습니다."),

    ARTICLE_NOT_FOUND(404, "ARTICLE_NOT_FOUND", "게시물이 존재하지 않습니다."),
    COMMENT_NOT_FOUND(404, "COMMENT_NOT_FOUND", "댓글이 존재하지 않습니다."),
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "사용자가 존재하지 않습니다.");

    private final int status;
    private final String error;
    private final String message;

}
