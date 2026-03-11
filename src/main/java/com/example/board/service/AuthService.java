package com.example.board.service;

import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.dto.request.AddUserRequest;
import com.example.board.dto.response.TokenResponse;
import com.example.board.repository.UserRepository;
import com.example.board.security.CustomUserDetails;
import com.example.board.security.jwt.JwtTokenProvider;
import com.example.board.security.jwt.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RedisTokenService redisTokenService;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public TokenResponse login(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        redisTokenService.saveRefreshToken(refreshToken, user.getId());

        System.out.println("AuthService.login");
        return new TokenResponse(accessToken, refreshToken);
    }

    public User save(AddUserRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + dto.getEmail());
        }
        return userRepository.save(
                User.builder()
                        .email(dto.getEmail())
                        .password(encoder.encode(dto.getPassword()))
                        .role(UserRole.USER)
                        .build()
        );
    }

    public void logout(User user, HttpServletResponse response) {

        String key = "refresh:"+ user.getId();
        redisTemplate.delete(key);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
