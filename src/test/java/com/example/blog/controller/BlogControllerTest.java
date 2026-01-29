package com.example.blog.controller;

import com.example.blog.domain.Article;
import com.example.blog.domain.User;
import com.example.blog.domain.UserRole;
import com.example.blog.dto.AddArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import com.example.blog.exception.ArticleNotFoundException;
import com.example.blog.repository.BlogRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.lang.runtime.ObjectMethods;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        blogRepository.deleteAll();
        userRepository.save(new User("test@example.com", "encoded-password", UserRole.ROLE));
    }

    @DisplayName("블로그 글 추가 성공")
    @WithMockUser(username = "test@example.com", roles = "USER")
    @Test
    public void addArticle() throws Exception {
        //given
        String url = "/api/articles";
        String title = "title";
        String content = "content";
        AddArticleRequest request = new AddArticleRequest(title, content);

        //객체를 json으로 직렬화
        String requestBody = objectMapper.writeValueAsString(request); //JSON으로 직렬화

        //when : API 요청
        ResultActions result = mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isCreated());

        List<Article> articleList = blogRepository.findAll();

        assertThat(articleList.size()).isEqualTo(1);
        assertThat(articleList.get(0).getTitle()).isEqualTo(title);
        assertThat(articleList.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("블로그 글 전체 조회 성공")
    @Test
    public void testFindAll() throws Exception {
        //given
        final String url = "/api/articles";
        final String title = "제목";
        final String content = "내용";
        Article article = blogRepository.save(new Article(title, content));

        //when
        ResultActions result = mockMvc.perform(get(url));

        //then : 정상적으로 요청이 되었는지 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(article.getTitle()))
                .andExpect(jsonPath("$[0].content").value(article.getContent()));
    }

    @DisplayName("특정 글 조회 성공")
    @Test
    public void testfindOne()  throws Exception {
        //given
        final String url = "/api/articles/{id}";
        final String title = "제목";
        final String content = "내용";
        Article article = blogRepository.save(new Article(title, content));
        Long savedId = article.getId();

        //when
        ResultActions result = mockMvc.perform(get(url, savedId));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.content").value(article.getContent()));
    }

    @DisplayName("블로그 전체 글 삭제 성공")
    @Test
    public void testDeleteAllArticle() throws Exception {
        // given: 테스트용 데이터 준비 (글 저장)
        final String url = "/api/articles";
        String title = "제목1";
        String content = "내용1";
        blogRepository.save(new Article(title, content));
        blogRepository.save(new Article(title, content));

        // when: 삭제 요청 수행
        mockMvc.perform(delete(url)).andExpect(status().isOk()); // delete(url, 변수1, ...)

        // then: 삭제 이후, 글이 실제로 삭제되었는지 검증
        List<Article> afterDeleteList = blogRepository.findAll();
        assertThat(afterDeleteList).isEmpty();
    }

    @DisplayName("블로그 글 삭제 성공")
    @Test
    public void testDeleteArticle() throws Exception {
        // given: 테스트용 데이터 준비 (글 저장)
        final String url = "/api/articles/{id}";
        String title = "제목1";
        String content = "내용1";
        Article article = blogRepository.save(new Article(title, content));
        Long savedId = article.getId();

        // when: 삭제 요청 수행
        mockMvc.perform(delete(url, savedId)).andExpect(status().isOk()); // delete(url, 변수1, ...)

        // then: 삭제 이후, 글이 실제로 삭제되었는지 검증
        List<Article> afterDeleteList = blogRepository.findAll();
        assertThat(afterDeleteList).isEmpty();
    }

    @DisplayName("블로그 수정 성공")
    @Test
    public void updateArticle() throws Exception {
        //given
        String url = "/api/articles/{id}";
        String title = "제목";
        String content = "내용";
        Article article = blogRepository.save(new Article(title, content));

        String modifiedTitle = "제목수정";
        String modifiedContent = "내용수정";
        UpdateArticleRequest request = new UpdateArticleRequest(modifiedTitle, modifiedContent);

        //when
        ResultActions result = mockMvc.perform(put(url, article.getId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        //then
        result.andExpect(status().isOk());

        Article afterModifiedArticle = blogRepository.findById(article.getId())
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));
        assertThat(afterModifiedArticle.getTitle()).isEqualTo(modifiedTitle);
        assertThat(afterModifiedArticle.getContent()).isEqualTo(modifiedContent);

    }
}