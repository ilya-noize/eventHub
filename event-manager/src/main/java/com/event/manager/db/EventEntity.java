package com.event.manager.db;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@NamedEntityGraph(
        name = "event.with.registrations.users",
        attributeNodes = @NamedAttributeNode(value = "registrations", subgraph = "registrations.userId"),
        subgraphs = @NamedSubgraph(
                name = "registrations.userId",
                attributeNodes = @NamedAttributeNode("userId")
        )
)

@Entity
@Table(name = "events")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "max_places", nullable = false)
    private Integer maxPlaces;

    @Column(name = "occupied_place", nullable = false)
    private Integer occupiedPlaces;

    @OneToMany(mappedBy = "event")
    private Set<EventRegistrationEntity> registrations;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="location_id")
    private LocationEntity location;

    @Column(name = "status", nullable = false)
    private String status;

    public EventEntity.EventEntityBuilder toBuilder() {
        return EventEntity.builder()
                .id(this.id)
                .name(this.name)
                .ownerId(this.ownerId)
                .maxPlaces(this.maxPlaces)
                .occupiedPlaces(this.occupiedPlaces)
                .date(this.date)
                .cost(this.cost)
                .duration(this.duration)
                .location(this.location)
                .status(this.status);
    }
}
