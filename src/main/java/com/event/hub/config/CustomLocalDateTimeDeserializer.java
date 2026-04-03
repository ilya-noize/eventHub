package com.event.hub.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String text = p.getText();
        try {
            // Парсим как OffsetDateTime, затем конвертируем в LocalDateTime
            return Instant.from(FORMATTER.parse(text)).atZone(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LocalDateTime from value '" + text + "'", e);
        }
    }
}