package com.event.manager.domain.location;

import com.event.manager.db.LocationEntity;
import com.event.manager.db.LocationRepository;
import com.event.manager.filter.LocationSearchFilter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("locationService")
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    private final LocationRepository locationRepository;

    @Transactional
    public LocationEntity save(LocationEntity entity) {
        log.debug("Save Location to DB: {}", entity.getName());
        return locationRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public LocationEntity getLocationById(Long id) {
        return findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such Location by ID:" + id)
        );
    }

    @Transactional(readOnly = true)
    public Page<LocationEntity> findAll(LocationSearchFilter filter) {
        return locationRepository.findAll(
                filter.toSpecification(),
                filter.toPageable()
        );
    }

    public boolean existsByNameOrAddress(LocationDto locationDto) {
        return locationRepository.existsByNameOrAddress(
                locationDto.name(),
                locationDto.address()
        );
    }

    public boolean existsByIdAndCapacityLessThan(Long locationId, Integer requestedPlace) {
        return locationRepository.existsByIdAndCapacityLessThan(locationId, requestedPlace);
    }

    public boolean existsById(Long id) {
        return locationRepository.existsById(id);
    }

    public boolean existsUnfinishedEventsInLocationById(Long id) {
        return locationRepository.existsUnfinishedEventsInLocationById(id);
    }

    public Optional<LocationEntity> findById(Long id) {
        return locationRepository.findById(id);
    }
}
