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

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM event_notifications n
            WHERE have_read = TRUE
                AND read_at <= NOW() - INTERVAL '7' DAY
            """, nativeQuery=true)
    void deleteReadingNotificationsAfterSevenDays();

    List<Notification> findAllByUserIdAndHaveReadFalse(Long userId);
}