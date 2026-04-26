package com.event.notifier.domain;

import com.event.notifier.db.NotificationEventPayloadRepository;
import com.event.notifier.db.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private final NotificationRepository notificationRepository;
    private final NotificationEventPayloadRepository notificationEventPayloadRepository;

    @Transactional
    @Scheduled(cron = "0 * * ? * *", zone = "Europe/Moscow")
    void deleteExpiredNotifications() {
        notificationRepository.deleteReadingNotificationsAfterSevenDays();
        notificationEventPayloadRepository.deleteOrphanedPayloads();
    }
}
