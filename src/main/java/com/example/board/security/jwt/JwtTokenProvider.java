package com.example.board.security.jwt;

import com.example.board.domain.User;
import com.example.board.exception.AuthException;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.UserRepository;
import com.example.board.security.CustomUserDetails;
import com.example.board.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private long accessTokenValidTime = 1000L * 60 * 3;
    private long refreshTokenValidTime = 1000L * 60 * 5;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
    }

    public String createAccessToken(User user) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("roles", List.of(user.getRole().name()))
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidTime))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(key)
                .compact();
    }

    //토큰 유효성 검증
    public void validateAccessToken(String token, String expectedType) {

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String type = claims.get("type", String.class);

        if (!expectedType.equals(type)) {
            throw new AuthException(Errorcode.INVALID_TOKEN);
        }
    }

    //요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public Authentication parseAuthentication(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        List<String> roles = claims.get("roles", List.class);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
