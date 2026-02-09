package com.example.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DB 저장
 */

@Entity
@Table(name = "refersh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    //유저와 연관관계
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RefreshToken(User user) {
        this.user = user;
    }

    public RefreshToken(User user, String token, LocalDateTime expiredAt) {
        this.user = user;
        this.token = token;
        this.expiredAt = expiredAt;
        this.createdAt = LocalDateTime.now();
    }

    //토큰 갱신
    public void updateToken(String token, LocalDateTime expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }

}
