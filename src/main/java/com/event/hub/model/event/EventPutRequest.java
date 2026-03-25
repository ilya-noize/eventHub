package com.event.hub.model.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventPutRequest(
        @NotBlank
        String name,

        @NotNull @Positive
        Integer maxPlaces,

        @NotNull @Future
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        LocalDateTime date,

        @NotNull @PositiveOrZero
        BigDecimal cost,

        @NotBlank
        String duration,

        @NotNull @Positive
        Long locationId
) {
}
