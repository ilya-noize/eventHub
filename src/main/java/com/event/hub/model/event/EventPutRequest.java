package com.event.hub.model.event;


import com.event.hub.config.CustomLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EventPutRequest(
        @NotBlank
        String name,

        @NotNull @Positive
        Integer maxPlaces,

        @NotNull @Future
        @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime date,

        @NotNull @Positive
        BigDecimal cost,

        @NotNull @Positive
        Integer duration,

        @NotNull @Positive
        Long locationId
) {
}
