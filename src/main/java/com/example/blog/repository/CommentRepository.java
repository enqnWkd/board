package com.example.blog.repository;

import com.example.blog.domain.Article;
import com.example.blog.domain.Comment;
import com.example.blog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticle(Article article);

}
