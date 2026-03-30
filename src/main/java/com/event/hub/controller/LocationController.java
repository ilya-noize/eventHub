package com.event.hub.controller;

import com.event.hub.filter.LocationSearchFilter;
import com.event.hub.model.location.LocationDto;
import com.event.hub.model.location.LocationMapper;
import com.event.hub.model.location.LocationPatchRequest;
import com.event.hub.model.location.LocationPostRequest;
import com.event.hub.model.location.LocationPutRequest;
import com.event.hub.model.location.LocationResponse;
import com.event.hub.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class LocationController {
    private final LocationMapper locationMapper;
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public LocationResponse createLocation(@RequestBody @Valid LocationPostRequest request) {
        log.info("Received a request to create a location {}", request.name());
        LocationDto domain = locationMapper.toDomain(request);

        return locationMapper.toResponse(locationService.createLocation(domain));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public LocationResponse updateLocation(
            @PathVariable Long id,
            @RequestBody @Valid LocationPutRequest request
    ) {
        log.info("Received a request to update a location by ID={}", id);
        LocationDto domain = locationMapper.toDomain(request);

        return locationMapper.toResponse(locationService.updateLocation(id, domain));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public LocationResponse patchLocation(
            @PathVariable Long id,
            @RequestBody @Valid LocationPatchRequest patchRequest
    ) {
        log.info("Received a request to patch a location by ID={}", id);
        LocationDto domain = locationMapper.toDomain(patchRequest);

        return locationMapper.toResponse(locationService.patchLocation(id, domain));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public LocationResponse getLocationById(@PathVariable Long id) {
        log.info("Received a request to get a location by ID={}", id);

        return locationMapper.toResponse(locationService.getLocationById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public Page<LocationResponse> getAllLocations(LocationSearchFilter filter) {
        log.info("Received a request to get a locations by filter={}", filter.toLogMessage());

        return locationService.getAllLocation(filter).map(locationMapper::toResponse);
    }
}
