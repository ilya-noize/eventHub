package com.event.notifier.kafka;

import com.event.common.event.EventNotificationPayload;
import com.event.notifier.domain.EventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventKafkaListener implements KafkaListenable<EventNotificationPayload> {
    private final EventNotificationService eventNotificationService;

    /**
     * Events topic listener
     *
     * @param consumerRecord EventNotificationPayload
     */
    @KafkaListener(
            topics = "event-topic",
            containerFactory = "containerEventFactory",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(
            ConsumerRecord<UUID, EventNotificationPayload> consumerRecord
    ) {
        UUID key = consumerRecord.key();
        log.info("Received event ID={} ", key);
        var value = consumerRecord.value();
        if (eventNotificationService.notExistsByKey(key)) {
            eventNotificationService.save(key, value);
        }
    }
}
