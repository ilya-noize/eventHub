package com.event.manager.domain;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventStatus;
import com.event.manager.domain.event.BatchEventStatusUpdaterService;
import com.event.manager.domain.event.EventNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class BatchEventStatusUpdater {

    private final BatchEventStatusUpdaterService batchEventStatusUpdaterService;
    private final EventNotifier notification;

    @Value("${batch.size}")
    private static final int BATCH_SIZE = 10;

    @Scheduled(cron = "0 * * ? * *", zone = "Europe/Moscow")
    @Transactional
    public void updateEventsToStarted() {
        String status = EventStatus.STARTED.name();
        log.info("Starting batch update of events to status: {}", status);

        int page = 0;
        int updatedTotal = 0;

        Slice<EventEntity> events;
        do {
            events = batchEventStatusUpdaterService.findAllMustBeStartedEvents(PageRequest.of(page, BATCH_SIZE));
            if (events.isEmpty()) break;

            int updated = batchEventStatusUpdaterService.updateStatusByIdsIn(
                    status,
                    events.getContent().stream().map(EventEntity::getId).distinct().toList()
            );
            updatedTotal += updated;
            log.debug("Updated {} events to STARTED (batch {})", updated, page);
            page++;

            sendNotification(status, events);
        } while (events.hasNext());

        if (updatedTotal > 0) {
            log.info("Batch update completed: {} events updated to STARTED", updatedTotal);
        }
    }

    @Scheduled(cron = "1 * * ? * *", zone = "Europe/Moscow")
    @Transactional
    public void updateEventsToFinished() {
        String status = EventStatus.FINISHED.name();
        log.info("Starting batch update of events to status: {}", status);

        int page = 0;
        int updatedTotal = 0;

        Slice<EventEntity> events;
        do {
            PageRequest pageable = PageRequest.of(page, BATCH_SIZE);
            events = batchEventStatusUpdaterService.findAllMustBeFinishedEvents(pageable);
            if (events.isEmpty()) break;

            int updated = batchEventStatusUpdaterService.updateStatusByIdsIn(
                    status,
                    events.getContent().stream().map(EventEntity::getId).distinct().toList()
            );
            updatedTotal += updated;
            log.debug("Updated {} events to FINISHED (batch {})", updated, page);
            page++;

            sendNotification(status, events);
        } while (events.hasNext());

        if (updatedTotal > 0) {
            log.info("Batch update completed: {} events updated to FINISHED", updatedTotal);
        }
    }

    private void sendNotification(String status, Slice<EventEntity> entities) {
        var parameters = EventNotifier.StatusUpdateNotificationParameters.builder()
                .status(status)
                .events(Set.copyOf(entities.getContent()))
                .build();
        notification.sendStatusUpdateNotification(parameters);
    }
}
