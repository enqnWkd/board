package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.exception.*;
import com.example.board.repository.ArticleLikeRepository;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleLikeRepository articleLikeRepository;

    public ArticleResponse save(AddArticleRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Article article = request.toEntity();
        article.setUser(user);
        articleRepository.save(article);

        return ArticleResponse.from(article, 0L);
    }

    public List<ArticleResponse> findAll() {

        List<Article> articles = articleRepository.findAll();

        return articles.stream()
                .map(article -> {
                    long likeCount = articleLikeRepository.countByArticleId(article.getId());
                    return new ArticleResponse(article, likeCount);
                })
                .toList();
    }

    public ArticleResponse findArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        Long likeCount = articleLikeRepository.countByArticleId(articleId);

        return ArticleResponse.from(article, likeCount);
    }

    public void deleteAll() {
        articleRepository.deleteAll();
    }

    public void delete(Long articleId, Long userId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        if (!article.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        articleLikeRepository.deleteByArticleId(articleId);

        articleRepository.deleteById(articleId);
    }

    public ArticleResponse update(Long articleId, UpdateArticleRequest request, Long userId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        if (!article.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        article.update(request.getTitle(), request.getContent());

        Long likeCount = articleLikeRepository.countByArticleId(articleId);

        return ArticleResponse.from(article, likeCount);
    }
}
