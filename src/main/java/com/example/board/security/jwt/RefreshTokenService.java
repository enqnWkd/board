package com.example.board.security.jwt;

import com.example.board.domain.RefreshToken;
import com.example.board.domain.User;
import com.example.board.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createRefreshToken(User user) {

        String token = jwtTokenProvider.createRefreshToken(user);

        LocalDateTime newExpiredAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidTime()));

        RefreshToken rt = refreshTokenRepository.findByUser(user)
                        .orElse(new RefreshToken(user));

        rt.updateToken(token, newExpiredAt);
        refreshTokenRepository.save(rt);

        return token;
    }
}
