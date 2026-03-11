package com.example.board.controller;

import com.example.board.security.CustomUserDetails;
import com.example.board.service.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleLikeController {

    private final ArticleLikeService articleLikeService;

    @PostMapping("/{articleId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long articleId, @AuthenticationPrincipal String email) {

        boolean liked = articleLikeService.toggleLike(articleId, email);

        return ResponseEntity.ok(liked);
    }
}
