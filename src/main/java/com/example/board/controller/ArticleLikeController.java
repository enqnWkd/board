package com.example.board.controller;

import com.example.board.security.CustomUserDetails;
import com.example.board.service.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Slf4j
public class ArticleLikeController {

    private final ArticleLikeService articleLikeService;

    @PostMapping("/{articleId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean liked = articleLikeService.toggleLike(articleId, userDetails.getUserId());

        return ResponseEntity.ok(liked);
    }

    @GetMapping("/{articleId}/like-count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long articleId) {
        Long count = articleLikeService.getLikeCount(articleId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{articleId}/liked-by-me")
    public ResponseEntity<?> isLikedByMe(
            @PathVariable Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean isLiked = articleLikeService.isLikedByUser(articleId, userDetails.getUserId());
        return ResponseEntity.ok(isLiked);
    }

}
