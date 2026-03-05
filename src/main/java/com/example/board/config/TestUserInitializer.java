package com.example.board.config;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class TestUserInitializer {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsersAndArticles() {

        return args -> {
            if (userRepository.findByEmail("a@a.com").isEmpty()) {
                User user1 = new User("a@a.com", passwordEncoder.encode("a"), UserRole.USER);
                user1 = userRepository.save(user1);

                boardRepository.save(new Article("제목1", "내용1", user1));
            }

            if (userRepository.findByEmail("aa@aa.com").isEmpty()) {
                User user2 = new User("aa@aa.com", passwordEncoder.encode("aa"), UserRole.USER);
                user2 = userRepository.save(user2);

                boardRepository.save(new Article("제목2", "내용2", user2));
            }
        };
    }

}
