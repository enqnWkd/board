package com.example.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AddCommentRequest {

    private String content;
    private Long articleId;

//    @AllArgsConstructor
    @Builder
    public AddCommentRequest(String content, Long articleId) {
        this.content = content;
        this.articleId = articleId;
    }
}
