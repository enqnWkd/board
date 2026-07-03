package com.example.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Long articleId;
    private Long senderId;
    private String content;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    @Builder
    public Notification(User receiver, NotificationType type, Long articleId, Long senderId, String content) {
        this.receiver = receiver;
        this.type = type;
        this.articleId = articleId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
