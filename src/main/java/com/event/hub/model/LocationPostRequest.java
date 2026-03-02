package com.event.hub.model;

import jakarta.validation.constraints.NotBlank;

public record LocationPostRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String capacity,
        @NotBlank String description
) {
}
