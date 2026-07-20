package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.NotificationType;
import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @MockitoBean
    private NotificationService notificationService;

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

        // 테스트 사용자 생성
        writer = userRepository.save(
                User.builder()
                .email("writer@test.com")
                .password("password")
                .build()
        );
    }

    @Test
    void 타인이_좋아요를_누르면_작성자에게_알림을_전송한다() {

        //given
        Article article = articleRepository.save(createArticle(writer));

        User user = userRepository.save(
                User.builder()
                .email("user@test.com")
                .password("password")
                .build()
        );

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), user.getId());

        //then
        verify(notificationService) //타인이 좋아요 누를시 알림 전송 확인
                .send(
                        eq(writer),
                        eq(NotificationType.LIKE),
                        eq(article.getId()),
                        eq(user.getId()),
                        any()
                );
    }

    @Test
    void 본인_게시글에_좋아요를_눌러도_알림은_전송되지_않는다() {

        //given
        Article article = articleRepository.save(createArticle(writer));

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), writer.getId());

        //then
        assertThat(result).isTrue();
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(1L);

        verify(notificationService, never())    //본인 좋아요는 알림 제외
                .send(any(), any(), any(), any(), any());
    }

    @Test
    void 좋아요를_누르면_Redis에_저장된다() {

        //given
        Article article = articleRepository.save(createArticle(writer));

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), writer.getId());

        //then
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(1L);
        assertThat(articleLikeService.isLikedByUser(article.getId(), writer.getId()))
                .isTrue();
    }


    @Test
    void 좋아요를_다시_누르면_취소된다() {
        //given
        Article article = articleRepository.save(createArticle(writer));

        articleLikeService.toggleLike(article.getId(), writer.getId());

        //when
        boolean result = articleLikeService.toggleLike(article.getId(), writer.getId());

        //then
        assertThat(result).isFalse();
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(0L);
    }

    @Test
    void 여러_사용자가_좋아요_누르면_좋아요수가_증가한다() {
        //given
        User user1 = userRepository.save(
                User.builder()
                        .email("userr1@test.com")
                        .password("password")
                        .build()
        );
        User user2 = userRepository.save(
                User.builder()
                        .email("userr2@test.com")
                        .password("password")
                        .build()
        );
        User user3 = userRepository.save(
                User.builder()
                        .email("userr3@test.com")
                        .password("password")
                        .build()
        );

        Article article = articleRepository.save(createArticle(writer));

        //when
        articleLikeService.toggleLike(article.getId(), user1.getId());
        articleLikeService.toggleLike(article.getId(), user2.getId());
        articleLikeService.toggleLike(article.getId(), user3.getId());

        //then
        assertThat(articleLikeService.getLikeCount(article.getId())).isEqualTo(3L);
        assertThat(articleLikeService.isLikedByUser(article.getId(), user1.getId())).isTrue();
        assertThat(articleLikeService.isLikedByUser(article.getId(), user2.getId())).isTrue();
        assertThat(articleLikeService.isLikedByUser(article.getId(), user3.getId())).isTrue();

    }

    @Test
    void Redis에_정상_저장() {
        //given
        Article article = articleRepository.save(createArticle(writer));

        //when
        articleLikeService.toggleLike(article.getId(), writer.getId());

        //then
        String likeKey = "likes:" + article.getId();
        Long count = redisTemplate.opsForSet().size(likeKey);

        assertThat(count).isEqualTo(1L);
    }

}
