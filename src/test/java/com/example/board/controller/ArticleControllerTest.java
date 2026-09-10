package com.example.board.controller;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.domain.UserRole;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.exception.ContentInspectionException;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.NotificationRepository;
import com.example.board.repository.UserRepository;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.ArticleContentInspectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private ArticleContentInspectionService articleContentInspectionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        articleRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
                .email("user@test.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        userRepository.save(testUser);
    }

    @DisplayName("게시글 추가 성공")
    @Test
    void addArticle() throws Exception {
        // given
        String url = "/api/articles";
        String title = "제목";
        String content = "내용";
        AddArticleRequest request = new AddArticleRequest(title, content);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        ResultActions result = mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                )
                        ))
        );

        result.andExpect(status().isCreated());
    }

    @DisplayName("게시글 목록 조회 성공 (페이징)")
    @WithMockUser(username = "test@example.com")
    @Test
    void findAllArticles() throws Exception {
        // given
        String url = "/api/articles";
        Article article1 = articleRepository.save(new Article("제목1", "내용1", testUser));
        Article article2 = articleRepository.save(new Article("제목2", "내용2", testUser));

        // when
        ResultActions result = mockMvc.perform(
                get(url + "?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        new CustomUserDetails(testUser),
                                        null,
                                        List.of()
                                )
                        ))
        );

        // then - Page 응답 형식 확인
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(article1.getTitle()))
                .andExpect(jsonPath("$.content[0].content").value(article1.getContent()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @DisplayName("게시글 상세 조회 성공")
    @WithMockUser(username = "test@example.com")
    @Test
    void findArticle() throws Exception {
        // given
        Article article = articleRepository.save(new Article("제목", "내용", testUser));
        String url = "/api/articles/{id}";

        // when
        ResultActions result = mockMvc.perform(
                get(url, article.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        new CustomUserDetails(testUser),
                                        null,
                                        List.of()
                                )
                        ))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.content").value(article.getContent()))
                .andExpect(jsonPath("$.likeCount").exists())
                .andExpect(jsonPath("$.likedByMe").exists());
    }

    @DisplayName("게시글 검색 성공")
    @WithMockUser(username = "test@example.com")
    @Test
    void searchArticles() throws Exception {
        // given
        String url = "/api/articles/search";
        articleRepository.save(new Article("Spring Boot 튜토리얼", "내용", testUser));
        articleRepository.save(new Article("Redis 캐싱", "내용", testUser));

        // when
        ResultActions result = mockMvc.perform(
                get(url + "?keyword=Spring&page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        new CustomUserDetails(testUser),
                                        null,
                                        List.of()
                                )
                        ))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot 튜토리얼"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @DisplayName("게시글 수정 성공")
    @WithMockUser(username = "test@example.com")
    @Test
    void updateArticle() throws Exception {
        // given
        Article article = articleRepository.save(new Article("원본 제목", "원본 내용", testUser));
        String url = "/api/articles/{id}";

        UpdateArticleRequest request = new UpdateArticleRequest("수정된 제목", "수정된 내용");
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                put(url, article.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        new CustomUserDetails(testUser),
                                        null,
                                        List.of()
                                )
                        ))
        );

        // then
        result.andExpect(status().isOk());

        Article updated = articleRepository.findById(article.getId())
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
    }

    @DisplayName("부적절한 게시글은 오류 응답을 반환한다")
    @Test
    void rejectInappropriateArticle() throws Exception {
        AddArticleRequest request = new AddArticleRequest("제목", "부적절한 내용");
        doThrow(new ContentInspectionException(Errorcode.INAPPROPRIATE_CONTENT))
                .when(articleContentInspectionService).inspect("제목", "부적절한 내용");

        mockMvc.perform(
                        post("/api/articles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(authentication(
                                        new UsernamePasswordAuthenticationToken(
                                                new CustomUserDetails(testUser),
                                                null,
                                                List.of()
                                        )
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INAPPROPRIATE_CONTENT"));

        assertThat(articleRepository.findAll()).isEmpty();
    }

    @DisplayName("게시글 삭제 성공")
    @WithMockUser(username = "test@example.com")
    @Test
    void deleteArticle() throws Exception {
        // given
        Article article = articleRepository.save(new Article("제목", "내용", testUser));
        String url = "/api/articles/{id}";

        // when
        ResultActions result = mockMvc.perform(
                delete(url, article.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        new CustomUserDetails(testUser),
                                        null,
                                        List.of()
                                )
                        ))
        );

        // then
        result.andExpect(status().isNoContent());

        List<Article> articles = articleRepository.findAll();
        assertThat(articles).isEmpty();
    }

}
