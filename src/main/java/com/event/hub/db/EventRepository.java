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

    @Query("""
        SELECT e FROM EventEntity e
            JOIN FETCH LocationEntity l ON e.location = l
            JOIN FETCH UserEntity u ON e.owner.id = :id
        """)
    Page<EventEntity> findByOwner_Id(Long id, Pageable pageable);

    @Query("""
            UPDATE EventEntity e
            SET e.occupiedPlaces = e.occupiedPlaces - 1
            WHERE e.id = :id
                 AND e.status = 'WAIT_START'
            """)
    @Modifying
    void freeUpOccupiedSpace(Long id);

    @Query("""
            UPDATE EventEntity e
            SET e.occupiedPlaces = e.occupiedPlaces + 1
            WHERE e.id = :id
                 AND e.maxPlaces > e.occupiedPlaces
                 AND e.status = 'WAIT_START'
            """)
    @Modifying
    int occupyEmptyPlace(Long id);

    @Query(value = """
            UPDATE events
            SET status = 'STARTED'
            WHERE date BETWEEN NOW() AND NOW() + CAST(duration AS INTERVAL)
                    AND status = 'WAIT_START';
            """, nativeQuery = true)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    int updateAllEventsStatusToStarted();

    @Query(value = """
            UPDATE events
            SET status = 'FINISHED'
            WHERE date <= (NOW() - CAST(duration AS INTERVAL)) 
                    AND status = 'STARTED';
            """, nativeQuery = true)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    int updateAllEventsStatusToFinished();

    @Query("""
            UPDATE EventEntity e
            SET e.status = 'CANCELED'
            WHERE e.id = :id
            """)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateEventStatusToCanceled(Long eventId);
}
