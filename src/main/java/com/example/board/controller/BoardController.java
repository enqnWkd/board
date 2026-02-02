package com.example.board.controller;

import com.example.board.domain.Article;
import com.example.board.dto.AddArticleRequest;
import com.example.board.dto.ArticleResponse;
import com.example.board.dto.UpdateArticleRequest;
import com.example.board.repository.UserRepository;
import com.example.board.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BoardController {
    private BlogService blogService;
    private UserRepository userRepository;

    public BoardController(BlogService blogService, UserRepository userRepository) {
        this.blogService = blogService;
        this.userRepository = userRepository;
    }

    //글 저장
    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(
            @RequestBody AddArticleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Article savedArticle = blogService.save(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    //전체 글 조회
    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> list = blogService.findAll()
                .stream().map(ArticleResponse::new)
                .toList();
        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    //특정 글 조회
    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") Long id) {
        ArticleResponse articleResponse = blogService.findArticle(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
        //return ResponseEntity.ok(articleResponse);
    }

    //전체 글 삭제
    @DeleteMapping("/api/articles")
    public ResponseEntity<Void> deleteAllArticles() {
        blogService.deleteAll();
        return ResponseEntity.ok().build();
    }

    //특정 글 삭제
    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        blogService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    //글 수정
    @Transactional
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable("id") Long id,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        Article updatedArticle = blogService.update(id, request,userDetails.getUsername());
        return ResponseEntity.ok(new ArticleResponse(updatedArticle));
    }
}
