package com.event.notifier.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificationEventPayloadRepository extends JpaRepository<NotificationEventPayload, Long> {
    boolean existsByMessageId(UUID key);

    @Modifying
    @Query("""
            DELETE FROM NotificationEventPayload p
            WHERE p.id IN (
                SELECT p2.id FROM NotificationEventPayload p2
                LEFT JOIN Notification n ON n.payload = p2
                WHERE n IS NULL
            )
            """)
    void deleteOrphanedPayloads();
}