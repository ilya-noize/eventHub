package com.event.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventUpdateStatusScheduler {
    private final EventService eventService;

    @Scheduled(cron = "0 * * ? * *", zone = "Europe/Moscow")
    public void updateAllEventsStatusToStarted() {
        eventService.updateAllEventsStatusToStarted();
    }

    @Scheduled(cron = "1 * * ? * *", zone = "Europe/Moscow")
    public void updateAllEventsStatusToFinished() {
        eventService.updateAllEventsStatusToFinished();
    }
}
