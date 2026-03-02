package com.event.hub.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationPutRequest(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String capacity,
        @NotBlank String description
) {
}
