package com.event.manager.domain.event;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EventDto {
    private Long id;
    private String name;
    private Long ownerId;
    private Integer maxPlaces;
    private Integer occupiedPlaces;
    private LocalDateTime date;
    private BigDecimal cost;
    private Integer duration;
    private Long locationId;
    private String status;

    public EventDto.EventDtoBuilder toBuilder() {
        return EventDto.builder()
                .id(this.id)
                .name(this.name)
                .ownerId(this.ownerId)
                .maxPlaces(this.maxPlaces)
                .occupiedPlaces(this.occupiedPlaces)
                .date(this.date)
                .cost(this.cost)
                .duration(this.duration)
                .locationId(this.locationId)
                .status(this.status);
    }
}

