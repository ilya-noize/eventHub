package com.event.hub.db;

import com.event.hub.db.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LocationRepository extends
        JpaRepository<LocationEntity, Long>,
        JpaSpecificationExecutor<LocationEntity> {

    boolean existsByNameOrAddress(String name, String address);
}
