package com.event.hub.model.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EventPutRequest(
        @NotBlank
        String name,

        @NotNull @Positive
        Integer maxPlaces,

        @NotNull @Future
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        LocalDateTime date,

        @NotNull @Positive
        BigDecimal cost,

        @NotNull @Positive
        Integer duration,

        @NotNull @Positive
        @PreAuthorize("@locationService.existsLocationById(#locationId)")
        Long locationId
) {
}
