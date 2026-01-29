package com.example.blog.dto;

import com.example.blog.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final String author;
    private final LocalDateTime createAt;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getUser().getEmail(),
            comment.getCreatedAt()
        );
    }

    public CommentResponse(Long id, String content, String author, LocalDateTime createAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createAt = createAt;
    }
}
