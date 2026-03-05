package com.example.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String refreshToken, Long userId) {
        String key = "refresh:" + userId;

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofSeconds(7)
        );
    }

}
