package com.event.hub.db;

import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventRegistrationEntity;
import com.event.hub.db.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Long> {

    Optional<EventRegistrationEntity> findByUser_IdAndEvent_Id(Long id, Long id1);

    @Query("""
        SELECT e FROM EventEntity e
        JOIN FETCH EventRegistrationEntity er ON (er.user = :user)
        """)
    Page<EventEntity> findAllByUser(UserEntity user, Pageable page);
}
