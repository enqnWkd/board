package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ArticleRepository articleRepository;

    private static final String VIEW_COUNT_KEY_PREFIX = "viewCount:";

    private Long getOrInitializeViewCount(Article article) {
        String viewKey = VIEW_COUNT_KEY_PREFIX + article.getId();
        String cached = redisTemplate.opsForValue().get(viewKey);

        //redis에 없다면 db값 사용
        if (cached == null) {
            long dbCount = article.getViewCount();
            redisTemplate.opsForValue().set(viewKey, String.valueOf(dbCount));
            return dbCount;
        } else {
            return Long.parseLong(cached);
        }
    }

    public Long increase(Article article) {
        getOrInitializeViewCount(article);

        String key = VIEW_COUNT_KEY_PREFIX + article.getId();

        return redisTemplate.opsForValue().increment(key);
    }

    public Long getViewCount(Article article) {
        return getOrInitializeViewCount(article);
    }

    public Map<Long, Long> getViewCounts(List<Article> articles) {
        Map<Long, Long> result = new HashMap<>();

        for (Article article : articles) {
            result.put(
                    article.getId(),
                    getOrInitializeViewCount(article)
            );
        }
        return result;
    }

    public void syncToDatabase() {

        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*");

        log.info("조회수 DB 동기화 시작 - 대상 {}건", keys.size());

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            Long articleId = Long.parseLong(
                    key.substring(VIEW_COUNT_KEY_PREFIX.length())
            );

            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                continue;
            }

            Long viewCount = Long.parseLong(value);

            articleRepository.updateViewCount(articleId, viewCount);

            log.info("조회수 DB 동기화 완료");
        }
    }
}
