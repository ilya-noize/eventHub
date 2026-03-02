package com.event.hub.model;

public record LocationResponse(
        Long id,
        String name,
        String address,
        String capacity,
        String description
) {
}
