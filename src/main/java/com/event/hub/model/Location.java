package com.event.hub.model;

public record Location(
        Long id,
        String name,
        String address,
        String capacity,
        String description
) {
}
