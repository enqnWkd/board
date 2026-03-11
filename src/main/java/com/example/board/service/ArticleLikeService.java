package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.ArticleLike;
import com.example.board.domain.User;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.ArticleLikeRepository;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long articleId, String email) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Optional<ArticleLike> like = articleLikeRepository.findByUserAndArticle(user, article);

        if(like.isPresent()) {
            articleLikeRepository.delete(like.get());
            return false;
        }

        ArticleLike articleLike = new ArticleLike(user, article);
        articleLikeRepository.save(articleLike);

        return true;
    }

    public Long getLikeCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        return articleLikeRepository.countByArticleId(articleId);
    }
}
