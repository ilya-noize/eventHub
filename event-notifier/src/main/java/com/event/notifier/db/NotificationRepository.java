package com.event.notifier.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.haveRead = TRUE
            WHERE n.userId = :userId AND n.id IN :ids
            """)
    void markNotificationAsRead(Long userId, List<Long> ids);

    List<Notification> findAllByUserId(Long userId);

    void deleteByHaveRead(boolean b);
}