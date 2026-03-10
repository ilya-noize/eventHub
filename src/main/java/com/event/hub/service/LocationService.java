package com.event.hub.service;

import com.event.hub.db.LocationRepository;
import com.event.hub.db.entity.LocationEntity;
import com.event.hub.filter.LocationSearchFilter;
import com.event.hub.model.Location;
import com.event.hub.model.LocationMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    public static final int PAGE_SIZE_LOCATION_MINIMAL = 3;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;


    public Location createLocation(Location location) {
        LocationEntity entity = locationMapper.toEntity(location);

        return saveAndMappedToDomain(entity);
    }

    public Location updateLocation(Long id, Location location) {
        isSameIds(id, location.id());
        existsLocationById(id);
        LocationEntity entity = locationMapper.toEntity(location);

        return saveAndMappedToDomain(entity);
    }

    public Location patchLocation(Long id, Location location) {
        isSameIds(id, location.id());
        LocationEntity entity = findLocationById(id);
        if (location.name() != null) {
            entity.setName(location.name());
        }
        if (location.address() != null) {
            entity.setAddress(location.address());
        }
        if (location.capacity() != null) {
            entity.setCapacity(location.capacity());
        }
        if (location.description() != null) {
            entity.setDescription(location.description());
        }

        return saveAndMappedToDomain(entity);
    }

    public void delete(Long id) {
        existsLocationById(id);
        locationRepository.deleteById(id);
    }

    public Location getLocationById(Long id) {
        LocationEntity entity = findLocationById(id);
        return locationMapper.toDomain(entity);
    }

    public Page<Location> getAllLocation(LocationSearchFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : PAGE_SIZE_LOCATION_MINIMAL;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);
        Page<LocationEntity> all = locationRepository.findAll(
                filter.toSpecification(),
                pageable
        );
        return all.map(locationMapper::toDomain);
    }

    private LocationEntity findLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such Location by ID:" + id)
        );
    }

    private Location saveAndMappedToDomain(LocationEntity entity) {
        return locationMapper.toDomain(locationRepository.save(entity));
    }

    private static void isSameIds(Long id, Long otherId) {
        if (!otherId.equals(id)) {
            throw new IllegalArgumentException("Location ID:%s and ID:%s must be same."
                    .formatted(otherId, id));
        }
    }

    private void existsLocationById(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new EntityNotFoundException("No such Location by ID:" + id);
        }
    }
}
