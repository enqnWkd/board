package com.example.board.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AddCommentRequest {

    private String content;
    private Long articleId;

}
