package com.event.notifier.kafka;

import com.event.common.registration.EventRegistrationNotificationPayload;
import com.event.notifier.domain.EventRegistrationNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventRegistrationKafkaListener implements KafkaListenable<EventRegistrationNotificationPayload> {
    private final EventRegistrationNotificationService eventRegistrationNotificationService;

    /**
     * Registration topic listener
     *
     * @param consumerRecord EventRegistrationNotificationPayload
     */
    @KafkaListener(
            topics = "registration-topic",
            containerFactory = "containerRegistrationFactory",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(
            ConsumerRecord<UUID, EventRegistrationNotificationPayload> consumerRecord
    ) {
        UUID key = consumerRecord.key();
        log.info("Received registration ID={} ", key);
        var value = consumerRecord.value();
        eventRegistrationNotificationService.save(value);
    }
}
