package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // DB 초기화
        articleRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = new User("test@example.com", "password", UserRole.USER);
        userRepository.save(testUser);
    }

    @Test
    void 게시글_목록_조회_페이징() {
        //given
        /*
        사용자와 불러올 게시글들 존재
         */
        for (int i=1; i<=25; i++) {
            articleRepository.save(new Article("제목"+i, "내용"+i, testUser));
        }

        //when
        /*
        전체 목록 조회 요청
         */
        Page<ArticleResponse> result = articleService.findAll(
                PageRequest.of(0, 20),
                testUser.getId()
        );

        //then
        assertThat(result.getContent()).hasSize(20);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    void 검색_기능() {
        //given
        /*
        사용자와 검색할 게시물 존재
         */
        articleRepository.save(new Article("Spring Boot", "내용", testUser));
        articleRepository.save(new Article("Redis", "내용", testUser));
        articleRepository.save(new Article("JPA", "내용", testUser));

        //when
        /*
        검색 요청
         */
        Page<ArticleResponse> result = articleService.search(
                "Spring",
                PageRequest.of(0, 20),
                testUser.getId()
        );

        //then
        //검색어가 맞는지 검증
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    void 게시글_생성_시_조회수_0() {
        Article article = articleRepository.save(
                new Article("title", "content", testUser)
        );

        //when
        Article saved = articleRepository.save(article);

        //then
        assertEquals(0L, saved.getViewCount());
        assertNotNull(saved.getId());
    }

}
