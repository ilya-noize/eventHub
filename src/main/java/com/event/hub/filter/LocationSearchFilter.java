package com.event.hub.filter;

import com.event.hub.db.entity.LocationEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.event.hub.service.LocationService.PAGE_SIZE_LOCATION_MINIMAL;

public record LocationSearchFilter(
        String name,
        String address,
        @Min(5)
        Integer capacity,
        String description,

        @Min(0)
        Integer pageNumber,

        @Min(PAGE_SIZE_LOCATION_MINIMAL)
        @Max(100)
        Integer pageSize
) {

    public Specification<LocationEntity> toSpecification() {
        return nameSpec()
                .and(addressSpec())
                .and(capacitySpec())
                .and(descriptionSpec());
    }

    public Specification<LocationEntity> nameSpec() {
        return ((root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.equal(root.get("name"), name)
                : null);
    }

    public Specification<LocationEntity> addressSpec() {
        return ((root, query, criteriaBuilder) -> address != null
                ? criteriaBuilder.equal(root.get("address"), address)
                : null);
    }

    public Specification<LocationEntity> capacitySpec() {
        return ((root, query, criteriaBuilder) -> capacity != null
                ? criteriaBuilder.equal(root.get("capacity"), capacity)
                : null);
    }

    public Specification<LocationEntity> descriptionSpec() {
        return ((root, query, criteriaBuilder) -> description != null
                ? criteriaBuilder.equal(root.get("description"), description)
                : null);
    }

    public String toLogMessage() {
        Map<String, String> params = new HashMap<>();

        try {
            Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(this);
                String valueStr = (value != null) ? value.toString() : "";

                params.put(fieldName, valueStr);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Ошибка при получении значений полей через рефлексию", e);
        }

        return params.entrySet().stream()
                .filter(entry ->
                        entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry ->
                        "%s:%s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }
}
