package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.NotificationType;
import com.example.board.domain.User;
import com.example.board.dto.response.NotificationResponse;
import com.example.board.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue())
                .willReturn(valueOperations);
    }

    @Test
    void 알림_전송시_지정된_유저에게_메세지가_전달된다() {

        //given
        given(valueOperations.increment(anyString()))
                .willReturn(1L);

        User receiver = User.builder()
                .email("receiver@test.com")
                .build();
        ReflectionTestUtils.setField(receiver, "id", 1L);

        //when
        notificationService.send(receiver, NotificationType.LIKE, 1L, 2L, "내용");

        //then
        verify(messagingTemplate).convertAndSendToUser(
                eq(receiver.getEmail()),
                eq("/notifications"),
                any(NotificationResponse.class)
        );

    }

    @Test
    void 알림_생성시_unread_count_증가() {

        //given
        given(valueOperations.increment(anyString()))
                .willReturn(1L);

        User receiver = User.builder()
                .email("receiver@test.com")
                .build();
        ReflectionTestUtils.setField(receiver, "id", 1L);

        //when
        notificationService.send(receiver, NotificationType.LIKE, 1L, 2L, "내용");

        //then
        verify(valueOperations)
                .increment(NotificationService.UNREAD_COUNT_KEY_PREFIX + receiver.getId());
    }

    @Test
    void 캐시가_있을때_DB를_조회하지_않는다() {

        //given
        Long userId = 1L;

        given(valueOperations.get(NotificationService.UNREAD_COUNT_KEY_PREFIX + userId))
                .willReturn("5");

        //when
        Long count = notificationService.getUnreadCount(userId); //cached != null

        //then
        assertThat(count).isEqualTo(5L);
        verify(notificationRepository, never())
                .countByReceiverIdAndIsReadFalse(userId); //DB 조회 안 했는지 확인
    }

    @Test
    void 캐시가_없을때_DB에서_계산해서_캐시를_채운다() {

        //given: redis 캐시가 없음
        Long userId = 1L;

        given(redisTemplate.opsForValue().get(NotificationService.UNREAD_COUNT_KEY_PREFIX + userId))
                .willReturn(null);

        given(notificationRepository.countByReceiverIdAndIsReadFalse(userId))
                .willReturn(3L); //읽지않은 알림 개수 3개

        //when
        Long count = notificationService.getUnreadCount(userId);

        //then
        assertThat(count).isEqualTo(3L);

        verify(notificationRepository)
                .countByReceiverIdAndIsReadFalse(userId);

        verify(valueOperations)
                .set(
                        NotificationService.UNREAD_COUNT_KEY_PREFIX + userId,
                        "3"
                );
    }

    @Test
    void 전체_읽음_처리시_캐시가_갱신된다() {

        //given
        Long userId = 1L;

        String key = NotificationService.UNREAD_COUNT_KEY_PREFIX + userId;

        given(notificationRepository.countByReceiverIdAndIsReadFalse(userId))
                .willReturn(0L);

        //when
        notificationService.markAllAsRead(userId);

        //then
        verify(notificationRepository).markAllAsRead(userId);
        verify(valueOperations).set(key, "0");
    }
}
