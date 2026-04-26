package com.event.manager.configuration;

import com.event.manager.domain.location.LocationDto;
import com.event.manager.domain.location.LocationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DefaultLocationInitializer {
    private final LocationService locationService;

    @PostConstruct
    public void init() {
        String locationJson = "location.json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource(locationJson);
            try (InputStream inputStream = resource.getInputStream()) {
                List<LocationDto> locations = mapper.readValue(inputStream,
                        mapper.getTypeFactory().constructCollectionType(List.class, LocationDto.class));
                locations.stream()
                        .filter(locationService::isUniqueNameAndAddressLocation)
                        .forEach(locationService::createLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("File " + locationJson + " not found:" + e.getMessage(), e);
        }
    }
}
