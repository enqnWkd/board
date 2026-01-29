package com.example.blog.dto;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class CommentRequest {
    private Long articleId;
    private String author;
    private String content;
}
