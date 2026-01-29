package com.example.blog.dto;

import com.example.blog.domain.Article;
import lombok.RequiredArgsConstructor;

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
