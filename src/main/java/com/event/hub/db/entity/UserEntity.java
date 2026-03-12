package com.event.hub.db.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usr")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends SuperEntity {
    @Column(name = "login", unique = true, nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "role", nullable = false)
    private String role;
}
