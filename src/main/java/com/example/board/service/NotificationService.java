package com.example.board.service;

import com.example.board.domain.Notification;
import com.example.board.domain.NotificationType;
import com.example.board.domain.User;
import com.example.board.dto.response.NotificationResponse;
import com.example.board.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String UNREAD_COUNT_KEY_PREFIX = "notification:unread:";

    @Transactional
    public void send(User receiver, NotificationType type, Long articleId, Long senderId, String content) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(type)
                .articleId(articleId)
                .senderId(senderId)
                .content(content)
                .build();

        notificationRepository.save(notification);

        //unread count 증가
        String key = UNREAD_COUNT_KEY_PREFIX + receiver.getId();
        redisTemplate.opsForValue().increment(key);

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/notifications", //앞에 "/user"가 자동으로 붙음
                NotificationResponse.from(notification)
        );
    }

    //알림 목록 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotification(Long userId, Pageable pageable) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);

        //unread count 초기화
        String key = UNREAD_COUNT_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(UNREAD_COUNT_KEY_PREFIX + userId, String.valueOf(key));
    }

    public Long getUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return Long.parseLong(cached);
        }

        //캐시 미스 -> DB에서 계산
        long count = notificationRepository.countByReceiverIdAndIsReadFalse(userId);
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }
}
