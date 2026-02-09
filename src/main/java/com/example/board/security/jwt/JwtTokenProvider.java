package com.example.board.security.jwt;

import com.example.board.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class JwtTokenProvider {

    private final SecretKey key;
    private long accessTokenValidTime = 1000L * 60;
    private long refreshTokenValidTime = 1000L * 60 * 60;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(User user) {
        Date now = new Date();

        return Jwts.builder()
                .subject(user.getUsername())
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
                .subject(user.getUsername())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(key)
                .compact();
    }

    public Long getRefreshTokenValidTime() {
        return refreshTokenValidTime;
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); //"Bearer " 이후 문자열만 추출
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();

        return new UsernamePasswordAuthenticationToken(
                username, null, authorities
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
