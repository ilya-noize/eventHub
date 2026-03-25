package com.event.hub.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_registration")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationEntity extends SuperEntity {

    @ManyToOne
    @JoinColumn(
            name="user_id",
            nullable = false,
            referencedColumnName = "id"
    )
    private UserEntity user;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(
            name="event_id",
            nullable=false,
            referencedColumnName = "id"
    )
    private EventEntity event;
}
