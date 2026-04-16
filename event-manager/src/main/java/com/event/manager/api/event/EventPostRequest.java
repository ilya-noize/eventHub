package com.event.manager.api.event;


import com.event.common.tool.CustomLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EventPostRequest(

        @NotBlank
        String name,

        @NotNull @Positive
        Integer maxPlaces,

        @NotNull @Future
        @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime date,

        @NotNull @PositiveOrZero
        BigDecimal cost,

        @NotNull
        Integer duration,

        @NotNull
        Long locationId
) {
}
