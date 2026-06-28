package com.example.board.service;

import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_KEY_PREFIX = "likes:";
    private static final long LIKE_EXPIRATION_DAYS = 30;

    @Transactional
    public boolean toggleLike(Long articleId, Long userId) {

        articleRepository.findById(articleId)
            .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        String likeKey = LIKE_KEY_PREFIX + articleId;
        String userIdStr = userId.toString();

        //redis 사용자가 이미 좋아요 했는지 확인
        Boolean alreadyLiked = redisTemplate.opsForSet()
                .isMember(likeKey, userIdStr);

        if (alreadyLiked != null && alreadyLiked) {
            //이미 좋아요한 경우: 제거
            redisTemplate.opsForSet().remove(likeKey, userIdStr);
            log.info("게시글 {} 좋아요 취소 - 사용자: {}", articleId, userId);
            return false;
        } else {
            //좋아요하지 않은 경우: 추가
            redisTemplate.opsForSet().add(likeKey, userIdStr);
            //TTL 설정
            redisTemplate.expire(likeKey, LIKE_EXPIRATION_DAYS, TimeUnit.DAYS);
            log.info("게시글 {} 좋아요 추가 - 사용자: {}", articleId, userId);
            return true;
        }
    }

    public Long getLikeCount(Long articleId) {
        String likeKey = LIKE_KEY_PREFIX + articleId;
        Long count = redisTemplate.opsForSet().size(likeKey);

        return count != null ? count : 0L;
    }

    public boolean isLikedByUser(Long articleId, Long userId) {
        String likeKey = LIKE_KEY_PREFIX + articleId;
        Boolean isLiked = redisTemplate.opsForSet()
                .isMember(likeKey, userId.toString());

        return isLiked != null && isLiked;
    }

    /**
     * 게시글 삭제 시 관련 좋아요 데이터 정리
     */
    public void deleteLikesForArticle(Long articleId) {
        String likeKey = LIKE_KEY_PREFIX + articleId;
        Boolean deleted = redisTemplate.delete(likeKey);
        log.info("게시글 {} 좋아요 데이터 삭제: {}", articleId, deleted);
    }

    // 여러 게시글의 좋아요 수를 한 번에 조회
    public Map<Long, Long> getLikeCountsForArticles(List<Long> articleIds) {
        Map<Long, Long> result = new HashMap<>();

        for (Long articleId : articleIds) {
            String likeKey = LIKE_KEY_PREFIX + articleId;
            Long count = redisTemplate.opsForSet().size(likeKey);
            result.put(articleId, count != null ? count : 0L);
        }

        return result;
    }

    // 특정 사용자가 어떤 게시글에 좋아요했는지 한 번에 조회
    public Map<Long, Boolean> getUserLikesForArticles(List<Long> articleIds, Long userId) {
        Map<Long, Boolean> result = new HashMap<>();
        String userIdStr = userId.toString();

        for (Long articleId : articleIds) {
            String likeKey = LIKE_KEY_PREFIX + articleId;
            Boolean isLiked = redisTemplate.opsForSet()
                    .isMember(likeKey, userIdStr);
            result.put(articleId, isLiked != null && isLiked);
        }

        return result;
    }

}
