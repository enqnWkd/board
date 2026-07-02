package com.example.board.service;

import com.example.board.domain.*;
import com.example.board.dto.request.AddCommentRequest;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    void 댓글_작성시_게시글_작성자에게_알림_전송() {
        //given
        User writer = User.builder()
                .email("writer@test.com")
                .build();
        ReflectionTestUtils.setField(writer, "id", 1L);

        Article article = Article.builder()
                .title("제목")
                .content("내용")
                .user(writer)
                .build();
        ReflectionTestUtils.setField(article, "id", 1L);

        User commenter = User.builder()
                .email("commenter@test.com")
                .build();
        ReflectionTestUtils.setField(article, "id", 2L);

        AddCommentRequest request = new AddCommentRequest("댓글내용");

        given(articleRepository.findById(1L))
                .willReturn(Optional.of(article));

        given(userRepository.findById(2L))
                .willReturn(Optional.of(commenter));

        given(commentRepository.save(any(Comment.class)))
                .willAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        //when
        commentService.save(1L, request, 2L);

        //then
        verify(notificationService).send(
                eq(writer),
                eq(NotificationType.COMMENT),
                eq(1L),
                eq(2L),
                anyString()
        );
    }

    @Test
    void 본인_글에는_댓글_알림을_보내지_않는다() {

        //given: article 작성자와 userId 동일
        Long articleId = 1L;
        Long userId = 1L;

        User writer = User.builder()
                .email("writer@test.com")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(writer, "id", userId);

        Article article = Article.builder()
                .title("제목")
                .content("내용")
                .user(writer)
                .build();
        ReflectionTestUtils.setField(article, "id", articleId);

        AddCommentRequest request = new AddCommentRequest("댓글내용");

        given(articleRepository.findById(articleId))
                .willReturn(Optional.of(article));

        given(userRepository.findById(userId))
                .willReturn(Optional.of(writer));

        given(commentRepository.save(any(Comment.class)))
                .willAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        //when
        commentService.save(articleId, request, userId);

        //then
        verify(notificationService, never()) //send()가 호출되지 않았는지 확인
                .send(any(), any(), any(), any(), any());

    }
}
