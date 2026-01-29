package com.example.blog.dto;

import com.example.blog.domain.Article;
import com.example.blog.domain.Comment;
import lombok.AllArgsConstructor;
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
