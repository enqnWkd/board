package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class ViewCountServiceTest {

    @Autowired
    private ViewCountService viewCountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private User writer;

    private Article createArticle(User writer) {
        return Article.builder()
                .title("제목")
                .content("내용")
                .user(writer)
                .build();
    }
    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();

        writer = userRepository.save(
                User.builder()
                        .email("writer@test.com")
                        .password("password")
                        .build()
        );
    }

    @Test
    void Redis에_조회수가_없으면_DB값으로_초기화된다() {

        //given
        Article article = articleRepository.save(createArticle(writer));
        article.updateViewCount(10L);

        String key = "viewCount:" + article.getId();
        assertThat(redisTemplate.opsForValue().get(key)).isNull();

        //when
        Long viewCount = viewCountService.getViewCount(article);

        //then
        assertThat(viewCount).isEqualTo(10L);
        assertThat(redisTemplate.opsForValue().get(key))
                .isEqualTo("10");
    }

    @Test
    void 조회수_정상_증가() {

        //given
        Article article = articleRepository.save(createArticle(writer));
        article.updateViewCount(10L);

        // when
        Long viewCount = viewCountService.increase(article);

        // then
        assertThat(viewCount).isEqualTo(11L);
        assertThat(redisTemplate.opsForValue().get("viewCount:" + article.getId()))
                .isEqualTo("11");
    }

    @Test
    void 여러_게시글_조회수_한번에_조회() {

        //given
        Article article1 = articleRepository.save(createArticle(writer));
        Article article2 = articleRepository.save(createArticle(writer));
        Article article3 = articleRepository.save(createArticle(writer));

        article1.updateViewCount(10L);
        article2.updateViewCount(20L);
        article3.updateViewCount(30L);

        redisTemplate.opsForValue().set("viewCount:" + article2.getId(), "100");

        //when
        Map<Long, Long> result = viewCountService.getViewCounts(List.of(article1, article2, article3));

        //then
        assertThat(result.get(article1.getId())).isEqualTo(10L);
        assertThat(result.get(article2.getId())).isEqualTo(100L);
        assertThat(result.get(article3.getId())).isEqualTo(30L);
    }

    @Test
    void 스케줄러_테스트() {

        //given
        Article article1 = articleRepository.save(createArticle(writer));
        Article article2 = articleRepository.save(createArticle(writer));

        redisTemplate.opsForValue().set("viewCount:" + article1.getId(), "10");
        redisTemplate.opsForValue().set("viewCount:" + article2.getId(), "20");

        //when
        viewCountService.syncToDatabase();

        entityManager.flush();
        entityManager.clear();

        //then
        Article updated1 = articleRepository.findById(article1.getId()).orElseThrow();
        Article updated2 = articleRepository.findById(article2.getId()).orElseThrow();

        assertThat(updated1.getViewCount()).isEqualTo(10L);
        assertThat(updated2.getViewCount()).isEqualTo(20L);

    }
}
