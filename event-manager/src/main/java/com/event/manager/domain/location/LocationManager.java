package com.event.manager.domain.location;

import com.event.manager.db.LocationEntity;
import com.event.manager.domain.LocationMapper;
import com.event.manager.filter.LocationSearchFilter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("locationManager")
@RequiredArgsConstructor
@Slf4j
public class LocationManager {
    private static final String CACHE_PREFIX_LOCATION = "location::";
    private final RedisTemplate<String, LocationEntity> redisTemplate;
    private final LocationService service;
    private final LocationMapper locationMapper;

    public LocationDto createLocation(LocationDto locationDto) {
        if (isUniqueNameAndAddressLocation(locationDto)) {
            throw new IllegalArgumentException("Duplicate Name or Address");
        }
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
        LocationEntity saved = service.save(entity);
        log.debug("Save Location to DB: {}", entity.getName());
        redisTemplate.opsForValue().getAndDelete(cacheKey(entity.getId()));
        return locationMapper.toDomain(saved);
    }

    @CacheEvict(value = CACHE_PREFIX_LOCATION, key = "#id")
    @Transactional
    public void delete(Long id) {
        if (existsLocationById(id) && !existsUnfinishedEventsInLocationById(id)) {
            service.delete(id);
        }
    }

    @Transactional(readOnly = true)
    public LocationDto getLocationById(Long id) {

        LocationEntity entity = findLocationById(id);
        return locationMapper.toDomain(entity);
    }

    //    @Cacheable(value = CACHE_PREFIX_LOCATION + "-search", key = "#filter")
    @Transactional(readOnly = true)
    public Page<LocationDto> getAllLocation(LocationSearchFilter filter) {
        return service.findAll(filter).map(locationMapper::toDomain);
    }

    public boolean isUniqueNameAndAddressLocation(LocationDto locationDto) {
        return service.existsByNameOrAddress(locationDto);
    }

    public boolean validateLocationFromRequestedPlaces(
            Long locationId,
            Integer requestedPlace
    ) {
        log.info("[validate] Checking availability of {} seats at a venue with an ID={}", requestedPlace, locationId);
        LocationEntity location = redisTemplate.opsForValue()
                .get(cacheKey(locationId));
        if (location == null) {
            if (service.existsByIdAndCapacityLessThan(locationId, requestedPlace)) {
                return true;
            }
        } else if (location.getCapacity() < requestedPlace) {
            return true;
        }
        throw new IllegalStateException(("There are not enough places in the selected " + CACHE_PREFIX_LOCATION + " ID=%s")
                .formatted(locationId)
        );
    }

    public boolean existsLocationById(Long id) {
        if (redisTemplate.hasKey(cacheKey(id))) {
            return true;
        } else if (service.existsById(id)) {
            return true;
        }
        throw new EntityNotFoundException("No such Location by ID:" + id);
    }

    public boolean existsUnfinishedEventsInLocationById(Long id) {
        if (service.existsUnfinishedEventsInLocationById(id)) {
            throw new IllegalStateException("There are unfinished events in the location by ID:" + id);
        }
        return false;
    }

    public LocationEntity findLocationById(Long id) {
        LocationEntity location = redisTemplate.opsForValue().get(cacheKey(id));
        return location != null
                ? location
                : service.getLocationById(id);
    }

    private static String cacheKey(Long locationId) {
        return (CACHE_PREFIX_LOCATION.concat("%s")).formatted(locationId);
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
