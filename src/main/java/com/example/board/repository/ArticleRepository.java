package com.example.board.repository;

import com.example.board.domain.Article;
import com.example.board.dto.response.ArticleResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

}
