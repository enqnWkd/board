package com.example.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer configure() {
        // 1.스프링 시큐리티의 모든 기능 비활성화
        return web -> web
                .ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers("/static/**"); //정적리소스 보안 제외
    }

    // 2.특정 HTTP 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .rememberMe(remember -> remember
                        .key("my-remember-key") // 토큰 암호화용 키
                        .tokenValiditySeconds(1209600) // 2주 (14일)
                        .rememberMeParameter("remember-me") // form input name
                        .userDetailsService(userDetailsService) // 사용자 정보 로드
                )
                // 3.인증, 인가 설정
                .authorizeHttpRequests(
                        auth -> auth
                        .requestMatchers("/test").authenticated()
//                        .requestMatchers("/admin").hasRole("ADMIN") //어드민 추가 시
                        .requestMatchers("/", "/login", "/signup", "/user").permitAll() //로그인, 회원가입은 누구나 허용
                        .anyRequest().authenticated() //그외는 인증 필요
                )
                // 4.폼 기반 로그인 설정
                .formLogin(
                        form -> form
                                .loginPage("/login") //기본 로그인 폼 말고 내가 만든 커스텀 로그인 페이지 경로
                                .defaultSuccessUrl("/articles", true) //로그인 성공 후 이동 경로
                                .permitAll()
                )
                // 5.로그아웃 설정
                .logout(
                        logout -> logout
//                        .logoutUrl("/logout")
                        .invalidateHttpSession(true) //로그아웃 시 세션 무효화
                        .deleteCookies("JSESSIONID") //브라우저 쿠키 삭제
                        .logoutSuccessUrl("/login") //로그아웃 후 이동 경로

                )

                //세션 관리
                .sessionManagement(
                        session -> session
//                                로그인하면 세션을 만들어서 인증 상태를 유지
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .invalidSessionUrl("/login?invalid") // 세션이 없거나 무효한 경우 리다이렉트
                )
                .sessionManagement(
                        session -> session
                                .maximumSessions(1) //동시 로그인 1개 제한
                                .maxSessionsPreventsLogin(false) //새로운 세션 생성 허용 + 이전 세션 만료
                                                                 //true면 추가 로그인 차단
                                .expiredUrl("/login?expired")
                )
                .csrf(csrf -> csrf.disable()) // 6.csrf 비활성화
//                .requiresChannel(
//                        https -> https
//                                .anyRequest()
//                                .requiresSecure() //모든 요청을 HTTPS로 요구
//                )
                .build();
    }

    // 9.패스워드 인코더로 사용할 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() { // bcrypt 해싱 알고리즘
        return new BCryptPasswordEncoder();
    }
}