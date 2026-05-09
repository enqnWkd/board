package com.example.board.dto.response;

import com.example.board.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
public class ArticleResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String email;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean likedByMe;

    public static ArticleResponse from(Article article, Long likeCount, boolean likedByMe) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .email(article.getUser().getEmail())
                .likeCount(likeCount)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .likedByMe(likedByMe)
                .build();
    }
}
