package com.example.board.controller;

import com.example.board.domain.Article;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;

    //게시글 등록
    @PostMapping
    public ResponseEntity<ArticleResponse> addArticle(
            @RequestBody AddArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        log.info("게시글 등록 요청 - userId: {}", userDetails.getUserId());

        ArticleResponse savedArticle = articleService.save(request, userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    //게시글 조회
    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> findAllArticles(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        log.debug("게시글 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Long userId = userDetails != null ? userDetails.getUserId() : null;
        Page<ArticleResponse> articles = articleService.findAll(pageable, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(articles);
    }

    //게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> findArticleWithView (
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("게시글 상세 조회 - articleId: {}", id);

        Long userId = userDetails != null ? userDetails.getUserId() : null;
        ArticleResponse articleResponse = articleService.findArticleWithViewIncrement(id, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
    }

    //게시글 상세 조회 - 조회수 증가 안 함 (캐시 조회 등에 사용)
    @GetMapping("/{id}/details")
    public ResponseEntity<ArticleResponse> findArticle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        ArticleResponse articleResponse = articleService.findArticle(id, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArticleResponse>> search(
            @RequestParam String keyword,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("게시글 검색 - keyword: {}, page: {}", keyword, pageable.getPageNumber(), pageable.getPageSize());

        Long userId = userDetails != null ? userDetails.getUserId() : null;
        Page<ArticleResponse> articles = articleService.search(keyword, pageable, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(articles);
    }

    //게시글 수정
    @Transactional
    @PutMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable("articleId") Long articleId,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("게시글 수정 요청 - articleId: {}, userId: {}", articleId, userDetails.getUserId());

        ArticleResponse updatedArticle = articleService.update(articleId, request, userDetails.getUserId());

        return ResponseEntity.ok(updatedArticle);
    }

    //게시글 삭제
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticles(
            @PathVariable("articleId") Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("게시글 삭제 요청 - articleId: {}, userId: {}", articleId, userDetails.getUserId());

        articleService.delete(articleId, userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllArticles() {
        log.warn("모든 게시글 삭제 요청");

        articleService.deleteAll();

        return ResponseEntity.noContent().build();
    }
}
