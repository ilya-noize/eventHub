package com.event.hub.db;

import com.event.hub.db.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends
        JpaRepository<EventEntity, Long>,
        JpaSpecificationExecutor<EventEntity> {

    Page<EventEntity> findByOwner_Id(Long id, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE EventEntity e
        SET e.occupiedPlaces = e.occupiedPlaces - 1
        WHERE e.id=:id
        """)
    void freeUpOccupiedSpace(Long id);

    @Modifying
    @Query("""
        UPDATE EventEntity e
        SET e.occupiedPlaces = e.occupiedPlaces + 1
        WHERE e.id=:id AND e.maxPlaces > e.occupiedPlaces
        """)
    boolean occupyEmptyPlace(Long id);
}
