package com.event.hub.model.location;

public record LocationResponse(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
