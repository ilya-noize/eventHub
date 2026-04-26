package com.event.manager.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends
        JpaRepository<EventEntity, Long>,
        JpaSpecificationExecutor<EventEntity> {

    @Query("SELECT e FROM EventEntity e WHERE e.id=:id AND e.status = 'WAIT_START'")
    @EntityGraph("event.with.registrations.users")
    Optional<EventEntity> findByIdWithRegistrations(Long id);

    Optional<EventEntity> findByIdAndStatus(Long id, String status);

    @Query("SELECT e FROM EventEntity e WHERE e.ownerId = :id")
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
            WHERE date BETWEEN NOW() AND NOW() + (duration * INTERVAL '1 minute')
                    AND status = 'WAIT_START';
            """, nativeQuery = true)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    int updateAllEventsStatusToStarted();

    @Query(value = """
            UPDATE events
            SET status = 'FINISHED'
            WHERE date <= (NOW() - (duration * INTERVAL '1 minute'))
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

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM events
            WHERE location_id = :locationId
              AND date + duration * INTERVAL '1 minute' > :dateStart
              AND date < :dateEnd
            """, nativeQuery = true)
    boolean isTimeConflictBeforeReservation(
            long locationId,
            LocalDateTime dateStart,
            LocalDateTime dateEnd
    );
}
