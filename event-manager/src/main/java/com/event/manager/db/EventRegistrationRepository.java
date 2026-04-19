package com.event.manager.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Long> {

    @Query("""
        SELECT e FROM EventEntity e
        JOIN FETCH EventRegistrationEntity er ON (er.event = e)
        WHERE er.userId = :userId
        """)
    Page<EventEntity> findAllEventWithRegistrations(Long userId, Pageable page);

    Optional<EventRegistrationEntity> findByUserIdAndEvent(Long id, EventEntity event);
}
