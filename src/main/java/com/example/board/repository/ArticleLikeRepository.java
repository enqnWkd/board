package com.example.board.repository;

import com.example.board.domain.Article;
import com.example.board.domain.ArticleLike;
import com.example.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    Optional findByUserAndArticle(User user, Article article);

    Long countByArticleId(Long articleId);
}
