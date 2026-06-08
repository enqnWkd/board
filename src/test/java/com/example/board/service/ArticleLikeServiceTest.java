package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ArticleLikeServiceTest {

    @Autowired
    private ArticleLikeService articleLikeService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();

        // 테스트 사용자 생성
        testUser = new User("test@example.com", "password", UserRole.USER);
        userRepository.save(testUser);
    }

    @Test
    void 좋아요_추가_테스트() {
        //given
        Article article = articleRepository.save(
                new Article("제목", "내용", testUser)
        );

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), testUser.getId());

        //then
        assertThat(result).isTrue();
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(1L);
    }

    @Test
    void 좋아요_취소_테스트() {
        //given
        Article article = articleRepository.save(
                new Article("제목", "내용", testUser)
        );
        articleLikeService.toggleLike(article.getId(), testUser.getId());

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), testUser.getId());

        //then
        assertThat(result).isFalse();
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(0L);
    }

    @Test
    void 중복_좋아요_방지() {
        //given
        Article article = articleRepository.save(
                new Article("제목", "내용", testUser)
        );

        //when - 좋아요 중복
        articleLikeService.toggleLike(article.getId(), testUser.getId());
        articleLikeService.toggleLike(article.getId(), testUser.getId());
        articleLikeService.toggleLike(article.getId(), testUser.getId());

        //then - 검증
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(1L);
    }

    @Test
    void 여러_사용자_좋아요() {
        //given
        User user1 = userRepository.save(new User("test1@ex.com", "pw", UserRole.USER));
        User user2 = userRepository.save(new User("test2@ex.com", "pw", UserRole.USER));
        User user3 = userRepository.save(new User("test3@ex.com", "pw", UserRole.USER));
        Article article = articleRepository.save(
                new Article("제목", "내용", user1)
        );

        //when
        articleLikeService.toggleLike(article.getId(), user1.getId());
        articleLikeService.toggleLike(article.getId(), user2.getId());
        articleLikeService.toggleLike(article.getId(), user3.getId());

        //then
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(3L);
        assertThat(articleLikeService.isLikedByUser(article.getId(), user1.getId()));
        assertThat(articleLikeService.isLikedByUser(article.getId(), user2.getId()));
        assertThat(articleLikeService.isLikedByUser(article.getId(), user3.getId()));

    }

    @Test
    void Redis에_정상_저장() {
        //given
        Article article = articleRepository.save(
                new Article("제목", "내용", testUser)
        );

        //when
        articleLikeService.toggleLike(article.getId(), testUser.getId());

        //then
        String likeKey = "likes:" + article.getId();
        Long count = redisTemplate.opsForSet().size(likeKey);

        assertThat(count).isEqualTo(1L);
    }

}
