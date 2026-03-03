package com.event.hub.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LocationPostRequest(
        @NotBlank
        String name,
        @NotBlank
        String address,
        @NotBlank
        @Min(5)
        Integer capacity,
        @NotBlank
        String description
) {
}
