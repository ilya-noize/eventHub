package com.event.hub.service.initializer;

import com.event.hub.model.location.Location;
import com.event.hub.service.LocationService;
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
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("location.json");
            try (InputStream inputStream = resource.getInputStream()) {
                List<Location> locations = mapper.readValue(inputStream,
                        mapper.getTypeFactory().constructCollectionType(List.class, Location.class));
                locations.stream()
                        .filter(locationService::isUniqueNameAndAddressLocation)
                        .forEach(locationService::createLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("File location.json not found:" + e.getMessage(), e);
        }
    }
}
