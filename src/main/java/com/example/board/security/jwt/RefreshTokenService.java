package com.example.board.security.jwt;

import com.example.board.domain.RefreshToken;
import com.example.board.domain.User;
import com.example.board.dto.response.TokenResponse;
import com.example.board.repository.RefreshTokenRepository;
import com.example.board.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public String createRefreshToken(User user) {

        String token = jwtTokenProvider.createRefreshToken(user);

        RefreshToken rt = refreshTokenRepository.findByUser(user)
                        .orElse(new RefreshToken(user));

        rt.updateToken(token, getNewExpiredAt());
        refreshTokenRepository.save(rt);

        return token;
    }

    private LocalDateTime getNewExpiredAt() {
        return LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidTime()));
    }

    public TokenResponse reissue(String requestRt) {

        Claims claims = jwtTokenProvider.parseClaimsAllowExpired(requestRt);
        String type = claims.get("type", String.class);

        if (!"REFRESH".equals(type)) {
            throw new JwtException("Invalid token type");
        }

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException());

        //DB상의 rt와 요청 rt가 같은지 비교
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException());

        //RT 만료 검사
        if (storedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new IllegalArgumentException();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        storedRefreshToken.updateToken(newRefreshToken, getNewExpiredAt());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
