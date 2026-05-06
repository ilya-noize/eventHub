package com.event.manager.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("SELECT e FROM EventEntity e WHERE e.date <= CURRENT_DATE AND e.status = 'WAIT_START'")
    @EntityGraph("event.with.registrations.users")
    @Deprecated
    List<EventEntity> findAllByStatusWaitStartAndDateLast(Pageable pageable);

    /**
     * Находит ID событий со статусом WAIT_START, у которых дата <= NOW()
     */
    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.registrations r ON r.event.id = e.id
            WHERE e.date <= CURRENT_DATE
               AND e.status = 'WAIT_START'
            """)
    Slice<EventEntity> findAllByStatusWaitStartAndDateBeforeNow(Pageable pageable);

    /**
     * Находит ID событий со статусом STARTED, у которых дата + duration < NOW()
     */
    @Query("""
            SELECT e FROM EventEntity e
            WHERE (e.date + (e.duration * INTERVAL '1 minute')) < CURRENT_TIMESTAMP
                AND e.status = 'STARTED'
            """)
    @EntityGraph("event.with.registrations.users")
    @Deprecated
    List<EventEntity> findAllByStatusStartedAndDateGone(Pageable pageable);

    /**
     * Находит ID событий со статусом STARTED, у которых дата + duration < NOW()
     */
    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.registrations r ON r.event.id = e.id
            WHERE (e.date + (e.duration * INTERVAL '1 minute')) < CURRENT_TIMESTAMP
                AND e.status = 'STARTED'
            """)
    Slice<EventEntity> findAllByStatusStartedAndDateExpired(Pageable pageable);

    /**
     * Обновляет статус по списку ID
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE EventEntity e SET e.status = :status WHERE e.id IN :ids")
    int updateStatusByIdsIn(String status, List<Long> ids);

    /**
     * Обновляет статус по ID
     */
    @Query("UPDATE EventEntity e SET e.status = :status WHERE e.id = :id")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateStatusById(Long id, String status);
}
