package com.event.manager.kafka;

import com.event.common.event.EventNotificationPayload;
import com.event.common.registration.EventRegistrationNotificationPayload;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfiguration {

    @Bean
    public KafkaTemplate<UUID, EventNotificationPayload> kafkaEventNotificationTemplate() {
        ProducerFactory<UUID, EventNotificationPayload> producerFactory =
                new DefaultKafkaProducerFactory<>(getProps());
        return new KafkaTemplate<>(producerFactory);
    }


    @Bean
    public KafkaTemplate<UUID, EventRegistrationNotificationPayload> kafkaEventRegistrationNotificationTemplate() {
        ProducerFactory<UUID, EventRegistrationNotificationPayload> producerFactory =
                new DefaultKafkaProducerFactory<>(getProps());
        return new KafkaTemplate<>(producerFactory);
    }

    private static Map<String, Object> getProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.UUIDSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JacksonJsonSerializer");
        return props;
    }
}
