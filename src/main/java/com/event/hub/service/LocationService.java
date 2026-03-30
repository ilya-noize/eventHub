package com.event.hub.service;

import com.event.hub.db.LocationRepository;
import com.event.hub.db.entity.LocationEntity;
import com.event.hub.filter.LocationSearchFilter;
import com.event.hub.model.location.LocationDto;
import com.event.hub.model.location.LocationMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("locationService")
@RequiredArgsConstructor
public class LocationService {
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;


    public LocationDto createLocation(LocationDto locationDto) {
        LocationEntity entity = locationMapper.toEntity(locationDto);

        return saveAndMappedToDomain(entity);
    }

    public LocationDto updateLocation(Long id, LocationDto locationDto) {
        LocationEntity existedLocation = findLocationById(id);
        LocationEntity entity = locationMapper.toEntity(locationDto);
        entity.setId(id);
        capacityNotLessThanBefore(entity.getCapacity(), existedLocation);
        return saveAndMappedToDomain(entity);
    }

    public LocationDto patchLocation(Long id, LocationDto locationDto) {
        isSameIds(id, locationDto.id());
        LocationEntity entity = findLocationById(id);
        if (isUniqueNameAndAddressLocation(locationDto)) {
            Optional.ofNullable(locationDto.name()).ifPresent(entity::setName);
            Optional.ofNullable(locationDto.address()).ifPresent(entity::setAddress);
        }
        Optional.ofNullable(locationDto.capacity()).ifPresent(capacity -> {
            capacityNotLessThanBefore(capacity, entity);
            entity.setCapacity(capacity);
        });
        Optional.ofNullable(locationDto.description()).ifPresent(entity::setDescription);

        return saveAndMappedToDomain(entity);
    }

    @Transactional
    public LocationDto saveAndMappedToDomain(LocationEntity entity) {
        return locationMapper.toDomain(locationRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (existsLocationById(id)) {
            locationRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public LocationDto getLocationById(Long id) {
        LocationEntity entity = findLocationById(id);
        return locationMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<LocationDto> getAllLocation(LocationSearchFilter filter) {
        Page<LocationEntity> all = locationRepository.findAll(
                filter.toSpecification(),
                filter.toPageable()
        );
        return all.map(locationMapper::toDomain);
    }

    public boolean isUniqueNameAndAddressLocation(LocationDto locationDto) {
        return !locationRepository.existsByNameOrAddress(
                locationDto.name(),
                locationDto.address()
        );
    }

    public boolean validateLocationFromRequestedPlaces(Long locationId, Integer requestedPlace) {
        if (locationRepository.existsByIdAndCapacityLessThan(locationId, requestedPlace)) {
            throw new IllegalStateException("There are not enough places in the selected location ID=%s"
                    .formatted(locationId)
            );
        }
        return true;
    }

    public boolean existsLocationById(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new EntityNotFoundException("No such Location by ID:" + id);
        }
        return true;
    }

    public LocationEntity findLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such Location by ID:" + id)
        );
    }

    private void capacityNotLessThanBefore(Integer capacity, LocationEntity entity) {
        if (entity.getCapacity() > capacity) {
            throw new IllegalStateException("New Capacity can't less than old one");
        }
    }

    private static void isSameIds(Long id, Long otherId) {
        if (!otherId.equals(id)) {
            throw new IllegalArgumentException("Location ID:%s and ID:%s must be same."
                    .formatted(otherId, id));
        }
    }
}
