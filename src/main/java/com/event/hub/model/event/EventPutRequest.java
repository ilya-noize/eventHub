package com.event.hub.model.event;


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
        LocalDateTime date,

        @NotNull @PositiveOrZero
        BigDecimal cost,

        @NotBlank
        String duration,

        @NotNull @Positive
        Long locationId
) {
}
