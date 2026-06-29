package com.example.board.dto.response;

import com.example.board.domain.Notification;
import com.example.board.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private final Long id;
    private final NotificationType type;
    private final Long articleId;
    private final Long senderId;
    private final String content;
    private final LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .articleId(notification.getArticleId())
                .senderId(notification.getSenderId())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
