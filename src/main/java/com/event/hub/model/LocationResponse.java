package com.event.hub.model;

public record LocationResponse(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
