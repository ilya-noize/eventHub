package com.event.hub.model.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String name;
    private Long ownerId;
    private Integer maxPlaces;
    private String occupiedPlaces;
    private LocalDateTime date;
    private BigDecimal cost;
    private String duration;
    private Long locationId;
    private String status;
}
