package com.example.board.repository;

import com.example.board.domain.RefreshToken;
import com.example.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    //로그아웃
    void deleteByUser(User user);
}
