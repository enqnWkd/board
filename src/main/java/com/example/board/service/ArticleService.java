package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.exception.*;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleLikeService articleLikeService;
    private final ViewCountService viewCountService;

    public ArticleResponse save(AddArticleRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Article article = request.toEntity();
        article.setUser(user);
        Article savedArticle = articleRepository.save(article);

        log.info("게시글 저장 완료 - articleId: {}", savedArticle.getId());

        return ArticleResponse.from(savedArticle, 0L, false, 0L);
    }

    public Page<ArticleResponse> findAll(Pageable pageable, Long userId) {
        log.debug("게시글 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Article> articles = articleRepository.findAllWithUser(pageable);

        List<Long> articleIds = articles.getContent().stream()
                .map(Article::getId)
                .collect(toList());

        Map<Long, Long> likeCounts = articleLikeService.getLikeCountsForArticles(articleIds);
        Map<Long, Boolean> userLikes = articleLikeService.getUserLikesForArticles(articleIds, userId);
        Map<Long, Long> viewCounts =
                viewCountService.getViewCounts(articles.getContent());

        return articles.map(article -> {
            Long likeCount = likeCounts.getOrDefault(article.getId(), 0L);
            Long viewCount = viewCounts.getOrDefault(article.getId(), article.getViewCount());
            boolean likedByMe = userId != null &&
            userLikes.getOrDefault(article.getId(), false);

            return ArticleResponse.from(article, likeCount, likedByMe, viewCount);
        });
    }

    @Transactional(readOnly = true)
    public ArticleResponse findArticle(Long articleId, Long userId) {

        Article article = articleRepository.findByIdWithUser(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        Long likeCount = articleLikeService.getLikeCount(articleId);
        boolean likedByMe = userId != null &&
                articleLikeService.isLikedByUser(articleId, userId);
        Long viewCount = viewCountService.getViewCount(article);

        return ArticleResponse.from(article, likeCount, likedByMe, viewCount);
    }


    public ArticleResponse findArticleWithViewIncrement(Long articleId, Long userId) {

        Article article = articleRepository.findByIdWithUser(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        Long viewCount = viewCountService.increase(article);

        Long likeCount = articleLikeService.getLikeCount(articleId);
        boolean likedByMe = userId != null &&
                articleLikeService.isLikedByUser(articleId, userId);

        log.info("게시글 조회 - articleId: {}, 조회수: {}", articleId, viewCount);

        return ArticleResponse.from(article, likeCount, likedByMe, viewCount);
    }


    public void deleteAll() {
        log.warn("모든 게시글 삭제");
        articleRepository.deleteAll();
    }

    public void delete(Long articleId, Long userId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        if (!article.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        articleLikeService.deleteLikesForArticle(articleId);

        articleRepository.deleteById(articleId);

        log.info("게시글 삭제 완료 - articleId: {}", articleId);
    }

    public ArticleResponse update(Long articleId, UpdateArticleRequest request, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        if (!article.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        article.update(request.getTitle(), request.getContent());

        //redis에서 좋아요 수 조회
        Long likeCount = articleLikeService.getLikeCount(articleId);
        boolean likedByMe = articleLikeService.isLikedByUser(articleId, userId);
        Long viewCount = viewCountService.getViewCount(article);

        log.info("게시글 수정 완료 - articleId: {}", articleId);

        return ArticleResponse.from(article, likeCount, likedByMe, viewCount);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> search(String keyword, Pageable pageable, Long userId) {
        log.debug("게시글 검색 - keyword: {}, page: {}", keyword, pageable.getPageNumber());

        Page<Article> articles = articleRepository.search(keyword, pageable);

        return articles.map(article -> {
            Long likeCount = articleLikeService.getLikeCount(article.getId());
            boolean likedByMe = userId != null &&
                    articleLikeService.isLikedByUser(article.getId(), userId);
            Long viewCount = viewCountService.getViewCount(article);

            return ArticleResponse.from(article, likeCount, likedByMe, viewCount);
        });
    }
}
