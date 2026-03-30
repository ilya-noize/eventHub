package com.event.hub.model.location;

public record LocationDto(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
