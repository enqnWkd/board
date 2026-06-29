package com.example.board.service;

import com.example.board.domain.Notification;
import com.example.board.domain.NotificationType;
import com.example.board.domain.User;
import com.example.board.dto.response.NotificationResponse;
import com.example.board.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void send(User receiver, NotificationType type, Long articleId, Long senderId, String content) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(type)
                .articleId(articleId)
                .senderId(senderId)
                .content(content)
                .build();

        notificationRepository.save(notification);

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
    }
}
