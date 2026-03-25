package com.event.hub.service;

import com.event.hub.db.LocationRepository;
import com.event.hub.db.entity.LocationEntity;
import com.event.hub.filter.LocationSearchFilter;
import com.event.hub.model.location.Location;
import com.event.hub.model.location.LocationMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;


    @Transactional
    public Location createLocation(Location location) {
        LocationEntity entity = locationMapper.toEntity(location);

        return saveAndMappedToDomain(entity);
    }

    @Transactional
    public Location updateLocation(Long id, Location location) {
        LocationEntity existedLocation = findLocationById(id);
        LocationEntity entity = locationMapper.toEntity(location);
        entity.setId(id);
        capacityNotLessThanBefore(entity.getCapacity(), existedLocation);
        return saveAndMappedToDomain(entity);
    }

    @Transactional
    public Location patchLocation(Long id, Location location) {
        isSameIds(id, location.id());
        LocationEntity entity = findLocationById(id);
        if (isUniqueNameAndAddressLocation(location)) {
            Optional.ofNullable(location.name()).ifPresent(entity::setName);
            Optional.ofNullable(location.address()).ifPresent(entity::setAddress);
        }
        Optional.ofNullable(location.capacity()).ifPresent(capacity -> {
            capacityNotLessThanBefore(capacity, entity);
            entity.setCapacity(capacity);
        });
        Optional.ofNullable(location.description()).ifPresent(entity::setDescription);

        return saveAndMappedToDomain(entity);
    }

    @Transactional
    public void delete(Long id) {
        existsLocationById(id);
        locationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Location getLocationById(Long id) {
        LocationEntity entity = findLocationById(id);
        return locationMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<Location> getAllLocation(LocationSearchFilter filter) {
        Page<LocationEntity> all = locationRepository.findAll(
                filter.toSpecification(),
                filter.toPageable()
        );
        return all.map(locationMapper::toDomain);
    }

    public boolean isUniqueNameAndAddressLocation(Location location) {
        return !locationRepository.existsByNameOrAddress(
                location.name(),
                location.address()
        );
    }

    /**
     * Verify that the number of available seats does not exceed those specified by a given
     */
    public Location getConfirmedLocationForEventCapacity(Long locationId, Integer eventMaxPlaces) {
        Location location = getLocationById(locationId);
        Integer locationCapacity = location.capacity();
        if (locationCapacity < eventMaxPlaces) {
            throw new IllegalArgumentException("%s places is more than %s capacity in %s place ID=%s"
                    .formatted(
                            eventMaxPlaces,
                            locationCapacity,
                            location.name(),
                            location.id()
                    ));
        }
        return location;
    }

    private static void capacityNotLessThanBefore(Integer capacity, LocationEntity entity) {
        if (entity.getCapacity() > capacity) {
            throw new IllegalStateException("New Capacity can't less than old one");
        }
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
