package com.event.notifier.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

@FunctionalInterface
public interface KafkaListenable<T> {
    void listen(ConsumerRecord<UUID, T> consumerRecord);
}
