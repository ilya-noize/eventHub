package com.event.hub.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "locs")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity extends SuperEntity {

    @Column(name = "name", nullable = false, unique = true,length = 64)
    private String name;

    @Column(name = "address", nullable = false, unique = true, length = 127)
    private String address;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description", nullable = false)
    private String description;
}
