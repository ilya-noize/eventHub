package com.event.manager.filter;

import com.event.manager.db.LocationEntity;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
public class LocationSearchFilter extends PageableFilter{
    private String name;
    private String address;
    @Min(5)
    private Integer capacity;
    private String description;

    public Specification<LocationEntity> toSpecification() {
        return nameSpec()
                .and(addressSpec())
                .and(capacitySpec())
                .and(descriptionSpec());
    }

    private Specification<LocationEntity> nameSpec() {
        return ((root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.equal(root.get("name"), name)
                : null);
    }

    private Specification<LocationEntity> addressSpec() {
        return ((root, query, criteriaBuilder) -> address != null
                ? criteriaBuilder.equal(root.get("address"), address)
                : null);
    }

    private Specification<LocationEntity> capacitySpec() {
        return ((root, query, criteriaBuilder) -> capacity != null
                ? criteriaBuilder.equal(root.get("capacity"), capacity)
                : null);
    }

    private Specification<LocationEntity> descriptionSpec() {
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
