package com.example.board.security.jwt;

import com.example.board.domain.User;
import com.example.board.dto.response.TokenResponse;
import com.example.board.exception.AuthException;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.UserRepository;
import com.example.board.service.RedisTokenService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedisTokenService redisTokenService;


    public String createRefreshToken(User user) {

        String token = jwtTokenProvider.createRefreshToken(user);

        String key = "refresh:" + user.getId();

        redisTemplate.opsForValue()
                .set(key, token, 7, TimeUnit.DAYS);
        return token;
    }

    public TokenResponse reissue(String requestRt) {

        Claims claims = jwtTokenProvider.parseClaimsAllowExpired(requestRt);

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new AuthException(Errorcode.INVALID_TOKEN);
        }

        String userId = claims.getSubject();
        String key = "refresh:" + userId;
        String stored = redisTemplate.opsForValue().get(key);

        if (stored == null) {
            throw new AuthException(Errorcode.INVALID_TOKEN);
        }

        if (!stored.equals(requestRt)) {
            //재사용 탐지 대응
            redisTemplate.delete(key);
            throw new AuthException(Errorcode.INVALID_TOKEN);
        }

        Boolean deleted = redisTemplate.delete(key);

        if (!deleted) {
            throw new AuthException(Errorcode.INVALID_TOKEN);
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        redisTokenService.saveRefreshToken(newRefreshToken, user.getId());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
