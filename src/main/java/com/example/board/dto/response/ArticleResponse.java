package com.example.board.dto;

import com.example.board.domain.Article;

public class ArticleResponse {

    private String title;
    private String content;
    private String email;

    public ArticleResponse(Article article) {
        this.title = article.getTitle();
        this.content = article.getContent();
        this.email = article.getUser().getEmail();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getEmail() {
        return email;
    }
}
