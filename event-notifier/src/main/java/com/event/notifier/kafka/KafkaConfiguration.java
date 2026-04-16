package com.event.notifier.kafka;

import com.event.common.event.EventNotificationPayload;
import com.event.common.registration.EventRegistrationNotificationPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;


    @Bean
    public ConsumerFactory<UUID, EventNotificationPayload> consumerEventFactory() {
        Map<String, Object> props = new HashMap<>();
        // TODO - override value from application.properties
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "notifier-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        var factory = new DefaultKafkaConsumerFactory<UUID, EventNotificationPayload>(props);
        factory.setValueDeserializer(new JacksonJsonDeserializer<>(EventNotificationPayload.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, EventNotificationPayload> containerEventFactory(
            ConsumerFactory<UUID, EventNotificationPayload> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<UUID, EventNotificationPayload>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<UUID, EventRegistrationNotificationPayload> consumerRegistrationFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "notifier-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        var factory = new DefaultKafkaConsumerFactory<UUID, EventRegistrationNotificationPayload>(props);
        factory.setValueDeserializer(new JacksonJsonDeserializer<>(EventRegistrationNotificationPayload.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, EventRegistrationNotificationPayload> containerRegistrationFactory(
            ConsumerFactory<UUID, EventRegistrationNotificationPayload> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<UUID, EventRegistrationNotificationPayload>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
