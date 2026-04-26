package com.event.manager.api.location;

public record LocationResponse(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
