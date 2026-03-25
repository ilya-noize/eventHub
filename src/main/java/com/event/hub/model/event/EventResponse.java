package com.event.hub.model.event;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse (
        Long id,
        String name,
        Long ownerId,
        Long locationId,
        LocalDateTime date,
        String duration,
        BigDecimal cost,
        String status,
        Integer maxPlaces,
        String occupiedPlaces
){
}
