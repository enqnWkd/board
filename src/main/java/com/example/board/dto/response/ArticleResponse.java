package com.example.board.dto.response;

import com.example.board.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ArticleResponse {

    private final String title;
    private final String content;
    private final String email;
    private final Long likeCount;

    public static ArticleResponse from(Article article, Long likeCount) {
        return new ArticleResponse(
                article.getTitle(),
                article.getContent(),
                article.getUser().getEmail(),
                likeCount
        );
    }

    public ArticleResponse(Article article, long likeCount) {
        this.title = article.getTitle();
        this.content = article.getContent();
        this.email = article.getUser().getEmail();
        this.likeCount = likeCount;
    }
}
