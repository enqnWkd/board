package com.example.board.security.jwt;

import com.example.board.exception.AuthException;
import com.example.board.exception.Errorcode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        //재발급 요청이면 AT 검증 건너뛰기
        if ("/auth/reissue".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                jwtTokenProvider.validateAccessToken(token, "ACCESS");
                Authentication authentication =
                        jwtTokenProvider.parseAuthentication(token);
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                if (!request.getRequestURI().startsWith("/auth")) {
                    throw new AuthException(Errorcode.EXPIRED_TOKEN);
                }
            } catch (JwtException e) {
                throw new AuthException(Errorcode.INVALID_TOKEN);
            }

        }
        filterChain.doFilter(request, response);
    }
}
