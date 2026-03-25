package com.example.board.controller;

import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ArticleController {
    private final ArticleService articleService;

    //게시글 등록
    @PostMapping("/articles")
    public ResponseEntity<ArticleResponse> addArticle(
            @RequestBody AddArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        ArticleResponse savedArticle = articleService.save(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    @GetMapping("/articles")
    public Page<ArticleResponse> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (keyword == null || keyword.isBlank()) {
            return articleService.findAll(pageable);
        }
        return articleService.search(keyword, pageable);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") Long id) {
        ArticleResponse articleResponse = articleService.findArticle(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
    }

    //게시글 삭제
    @DeleteMapping("/articles")
    public ResponseEntity<Void> deleteAllArticles() {
        articleService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<Void> deleteArticles(
            @PathVariable("articleId") Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        articleService.delete(articleId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    //게시글 수정
    @Transactional
    @PutMapping("/articles/{articleId}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable("articleId") Long articleId,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        ArticleResponse updatedArticle = articleService.update(articleId, request, userDetails.getUserId());
        return ResponseEntity.ok(updatedArticle);
    }

}
