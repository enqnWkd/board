package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ArticleRepository articleRepository;

    private static final String VIEW_COUNT_KEY_PREFIX = "viewCount:";

    public Long getViewCount(Article article) {
        String viewKey = VIEW_COUNT_KEY_PREFIX + article.getId();
        String cached = redisTemplate.opsForValue().get(viewKey);

        //redis에 없다면 db값 사용
        if (cached == null) {
            long dbCount = article.getViewCount();
            redisTemplate.opsForValue().set(viewKey, String.valueOf(dbCount));
            return dbCount;
        }

        return Long.parseLong(cached);
    }

    public Long increase(Article article) {
        getViewCount(article);

        String key = VIEW_COUNT_KEY_PREFIX + article.getId();

        return redisTemplate.opsForValue().increment(key);
    }

    public Map<Long, Long> getViewCounts(List<Article> articles) {
        Map<Long, Long> result = new HashMap<>();

        for (Article article : articles) {
            result.put(
                    article.getId(),
                    getViewCount(article)
            );
        }
        return result;
    }

    public void syncToDatabase() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(VIEW_COUNT_KEY_PREFIX + "*")
                .count(100)
                .build();

        redisTemplate.execute((RedisCallback<Void>) connection -> {

            int updatedCount = 0;

            try (Cursor<byte[]> cursor = connection.scan(options)) {

                while (cursor.hasNext()) {

                    byte[] keyBytes = cursor.next();
                    String key = new String(keyBytes, StandardCharsets.UTF_8);

                    Long articleId = Long.parseLong(
                            key.substring(VIEW_COUNT_KEY_PREFIX.length())
                    );

                    byte[] valueBytes = connection.get(keyBytes);

                    if (valueBytes == null) {
                        continue;
                    }

                    Long viewCount = Long.parseLong(
                            new String(valueBytes, StandardCharsets.UTF_8)
                    );

                    articleRepository.updateViewCount(articleId, viewCount);
                    updatedCount++;
                }
            }

            log.info("조회수 DB 동기화 완료 - {}건", updatedCount);

            return null;
        });
    }
}