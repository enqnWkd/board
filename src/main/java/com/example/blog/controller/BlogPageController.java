package com.example.blog.controller;

import com.example.blog.domain.Article;
import com.example.blog.dto.ArticleViewResponse;
import com.example.blog.dto.CommentResponse;
import com.example.blog.service.BlogService;
import com.example.blog.service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BlogPageController {
    private final BlogService blogService;
    private final CommentService commentService;

    public BlogPageController(BlogService blogService, CommentService commentService) {
        this.blogService = blogService;
        this.commentService = commentService;
    }

    // 글 목록 조회
    @GetMapping("/articles")
    public String getArticles(Model model, HttpSession session, @AuthenticationPrincipal User user) {
        System.out.println("세션 ID: " + session.getId());
        System.out.println("로그인 사용자: " + user.getUsername());

        List<ArticleViewResponse> articles = blogService.findAll().stream()
                .map(ArticleViewResponse::new)
                .toList();
        model.addAttribute("articles", articles); //modle에 블로그 글 리스트 저장

        return "articleList";
    }

    // 글 상세 조회
    @GetMapping("/articles/{id}")
    public String showArticle(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails //현재 로그인한 유저 정보
    ) {
        Article article = blogService.findById(id); //글 정보 - 작성자
        List<CommentResponse> comments = commentService.getCommentsByArticle(id);
        String loginEmail = userDetails != null ? userDetails.getUsername() : null;

//        model.addAttribute("article", new ArticleViewResponse(article));
        model.addAttribute("article", new ArticleViewResponse(article));
        model.addAttribute("comments", comments);
        model.addAttribute("loginEmail", loginEmail); //현재 로그인한 유저 이메일

        return "article";
    }

    // 글 수정/생성
    @GetMapping("/new-article")
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("article", new ArticleViewResponse());
        }
        else {
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article));
        }
        return "newArticle";
    }
}
