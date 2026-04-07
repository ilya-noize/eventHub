package com.event.hub.model.location;

import lombok.Builder;

@Builder
public record LocationDto(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
