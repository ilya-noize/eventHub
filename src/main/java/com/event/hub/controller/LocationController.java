package com.event.hub.controller;

import com.event.hub.filter.LocationSearchFilter;
import com.event.hub.model.Location;
import com.event.hub.model.LocationMapper;
import com.event.hub.model.LocationPostRequest;
import com.event.hub.model.LocationResponse;
import com.event.hub.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationMapper locationMapper;
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse createLocation(
            @RequestBody LocationPostRequest request
    ) {
        Location createdLocation = locationService.createLocation(
                locationMapper.toDomain(request)
        );
        return locationMapper.toResponse(createdLocation);
    }

    @PutMapping("/{id}")
    public LocationResponse updateLocation(
            @PathVariable Long id,
            @RequestBody LocationPostRequest request
    ) {
        Location updatedLocation = locationService.updateLocation(
                id, locationMapper.toDomain(request)
        );
        return locationMapper.toResponse(updatedLocation);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
    }

    @GetMapping("/{id}")
    public LocationResponse getLocationById(@PathVariable Long id) {
        return locationMapper.toResponse(
                locationService.getLocationById(id)
        );
    }

    @GetMapping
    public Page<LocationResponse> getAllLocations(LocationSearchFilter filter) {
        return locationService.getAllLocation(filter).map(locationMapper::toResponse);
    }
}
