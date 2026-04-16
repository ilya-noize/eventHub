package com.event.manager.kafka;

@FunctionalInterface
public interface NotificationSender<T> {

    void send(T payload);
}
