package com.event.hub.model;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record LocationPatchRequest(
        @NotNull
        Long id,
        Optional<String> name,
        Optional<String> address,
        Optional<Integer> capacity,
        Optional<String> description
) {
}
