package com.event.hub.model.event;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EventResponse (
        Long id,
        String name,
        Long ownerId,
        Long locationId,
        LocalDateTime date,
        Integer duration,
        BigDecimal cost,
        String status,
        Integer maxPlaces,
        Integer occupiedPlaces
){
}
