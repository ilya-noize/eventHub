package com.event.hub.db.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity extends SuperEntity {

    @Column(name = "", nullable = false)
    private String name;

    @ManyToOne
    private UserEntity owner;

    @Column(name = "max_places", nullable = false)
    private String maxPlaces;

    @Column(name = "occupied_places", nullable = false)
    private String occupiedPlaces;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "cost", nullable = false)
    private String cost;

    @Column(name = "duration", nullable = false)
    private String duration;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private LocationEntity location;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.WAIT_START;
}
