package com.event.hub.db;

import com.event.hub.db.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LocationRepository extends
        JpaRepository<LocationEntity, Long>,
        JpaSpecificationExecutor<LocationEntity> {

    boolean existsByNameOrAddress(String name, String address);

    boolean existsByIdAndCapacityLessThan(Long id, Integer requestedPlace);

    @Query(value = """
            SELECT COUNT(e) > 0 FROM EventEntity e
                WHERE e.location.id = :id
                  AND e.status NOT IN ('FINISHED', 'CANCELED')
            """)
    boolean existsUnfinishedEventsInLocationById(Long id);
}
