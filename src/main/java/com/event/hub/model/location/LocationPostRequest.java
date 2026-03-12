package com.event.hub.model.location;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationPostRequest(
        @NotBlank
        @Size(max = 64)
        String name,

        @NotBlank
        @Size(max = 127)
        String address,

        @NotBlank
        @Min(5)
        Integer capacity,

        @NotBlank
        @Size(min=50, max=500)
        String description
) {
}
