package com.example.board.repository;

import com.example.board.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Modifying //벌크 쿼리
    @Query("update Notification n set n.isRead = true where n.receiver.id = :receiverId and n.isRead = false")
    int markAllAsRead(@Param("receiverId") Long receiverId);
}