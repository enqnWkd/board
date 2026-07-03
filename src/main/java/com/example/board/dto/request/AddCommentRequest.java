package com.example.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class AddCommentRequest {

    private String content;

    public AddCommentRequest(String content) {
        this.content = content;
    }
}
