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

    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getTitle(),
                article.getContent(),
                article.getUser().getEmail()
        );
    }

    public ArticleResponse(Article article) {
        this.title = article.getTitle();
        this.content = article.getContent();
        this.email = article.getUser().getEmail();
    }
}
