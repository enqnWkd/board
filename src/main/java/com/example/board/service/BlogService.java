package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.dto.AddArticleRequest;
import com.example.board.dto.ArticleResponse;
import com.example.board.dto.UpdateArticleRequest;
import com.example.board.exception.*;
import com.example.board.repository.BlogRepository;
import com.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public BlogService(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    public Article save(AddArticleRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Article article = request.toEntity();
        article.setUser(user);
        return blogRepository.save(article);
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public ArticleResponse findArticle(Long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));
        return new ArticleResponse(article);
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));
    }

    public void deleteAll() {
        blogRepository.deleteAll();
    }

    public void delete(Long id, String email) {

        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        if (!article.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }
        blogRepository.deleteById(id);
    }

    //수정 로직
    @Transactional
    public Article update(Long id, UpdateArticleRequest request, String email) {

        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        if (!article.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        article.update(request.getTitle(), request.getContent());
        return article;
    }
}
