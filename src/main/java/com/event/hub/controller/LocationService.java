package com.event.hub.controller;

import com.event.hub.entity.LocationEntity;
import com.event.hub.model.Location;
import com.event.hub.model.LocationMapper;
import com.event.hub.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;


    public Location createLocation(Location location) {
        LocationEntity entity = locationMapper.toEntity(location);
        LocationEntity saved = locationRepository.save(entity);

        return locationMapper.toDomain(saved);
    }

    public Location updateLocation(Long id, Location location) {
        LocationEntity entity = locationMapper.toEntity(location);
        if(!location.id().equals(id)) {
            throw new IllegalArgumentException("Location ID:%s and ID:%s must be same."
                    .formatted(location.id(), id));
        }
        existsLocationById(id);
        LocationEntity updated = locationRepository.save(entity);

        return locationMapper.toDomain(updated);
    }

    public Location getLocationById(Long id) {
        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(getEntityNotFoundExceptionSupplier(id));

        return locationMapper.toDomain(entity);
    }

    public Page<Location> getAllLocation(Pageable pageable) {
        Page<LocationEntity> all = locationRepository.findAll(pageable);

        return all.map(locationMapper::toDomain);
    }

    public void delete(Long id) {
        existsLocationById(id);
        locationRepository.deleteById(id);
    }

    private void existsLocationById(Long id) {
        if(!locationRepository.existsById(id)) {
            getEntityNotFoundExceptionSupplier(id);
        }
    }

    private static Supplier<EntityNotFoundException> getEntityNotFoundExceptionSupplier(Long id) {
        return () -> new EntityNotFoundException("No such Location by ID:" + id);
    }
}
