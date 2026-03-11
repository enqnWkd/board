package com.example.board.controller;

import com.example.board.domain.Article;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ArticleController {
    private final ArticleService articleService;

    //게시글 등록
    @PostMapping("/api/articles")
    public ResponseEntity<ArticleResponse> addArticle(
            @RequestBody AddArticleRequest request,
            @AuthenticationPrincipal String email
    ) {
        ArticleResponse savedArticle = articleService.save(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    //게시글 조회
    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> list = articleService.findAll();

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") Long id) {
        ArticleResponse articleResponse = articleService.findArticle(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
    }

    //게시글 삭제
    @DeleteMapping("/api/articles")
    public ResponseEntity<Void> deleteAllArticles() {
        articleService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        articleService.delete(id, email);
        return ResponseEntity.ok().build();
    }

    //게시글 수정
    @Transactional
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable("id") Long id,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal String email
            ) {
        ArticleResponse updatedArticle = articleService.update(id, request, email);
        return ResponseEntity.ok(updatedArticle);
    }
}
