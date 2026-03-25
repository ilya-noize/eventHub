package com.event.hub.filter;

import com.event.hub.db.entity.EventEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchFilter extends PageableFilter {
    private String durationMax;
    private String durationMin;
    private Integer placesMax;
    private Integer placesMin;
    private BigDecimal costMax;
    private BigDecimal costMin;
    private LocalDateTime dateStartBefore;
    private LocalDateTime dateStartAfter;
    private String name;
    private Long locationId;
    private String eventStatus;
    private Long id;
    private Long ownerId;

    public Specification<EventEntity> toSpecification() {
        return equalIdSpec()
                .and(equalOwnerSpec())
                .and(equalStatusSpec())
                .and(likeNameSpec())
                .and(equalLocationSpec())
                .and(greaterDateStartAfterSpec().and(lessDateStartBeforeSpec()))
                .and(lessCostMaxSpec().and(greaterCostMinSpec()))
                .and(lessPlacesMaxSpec().and(greaterPlacesMinSpec()))
                .and(lessDurationMaxSpec().and(greaterDurationMinSpec()));
    }

    public Specification<EventEntity> lessDurationMaxSpec() {
        return ((eventEntity, query, cb) -> durationMax != null
                ? cb.lessThanOrEqualTo(eventEntity.get("duration"), durationMax)
                : null
        );
    }

    public Specification<EventEntity> greaterDurationMinSpec() {
        return ((eventEntity, query, cb) -> durationMin != null
                ? cb.greaterThanOrEqualTo(eventEntity.get("duration"), durationMin)
                : null
        );
    }

    public Specification<EventEntity> lessPlacesMaxSpec() {
        return ((eventEntity, query, cb) -> placesMax != null
                ? cb.lessThanOrEqualTo(eventEntity.get("places"), placesMax)
                : null
        );
    }

    public Specification<EventEntity> greaterPlacesMinSpec() {
        return ((eventEntity, query, cb) -> placesMin != null
                ? cb.greaterThanOrEqualTo(eventEntity.get("places"), placesMin)
                : null
        );
    }

    public Specification<EventEntity> lessCostMaxSpec() {
        return ((eventEntity, query, cb) -> costMax != null
                ? cb.lessThanOrEqualTo(eventEntity.get("cost"), costMax)
                : null
        );
    }

    public Specification<EventEntity> greaterCostMinSpec() {
        return ((eventEntity, query, cb) -> costMin != null
                ? cb.greaterThanOrEqualTo(eventEntity.get("cost"), costMin)
                : null
        );
    }

    public Specification<EventEntity> lessDateStartBeforeSpec() {
        return ((eventEntity, query, cb) -> dateStartBefore != null
                ? cb.lessThanOrEqualTo(eventEntity.get("date"), dateStartBefore)
                : null
        );
    }

    public Specification<EventEntity> greaterDateStartAfterSpec() {
        return ((eventEntity, query, cb) -> dateStartAfter != null
                ? cb.greaterThanOrEqualTo(eventEntity.get("date"), dateStartAfter)
                : null
        );
    }

    public Specification<EventEntity> likeNameSpec() {
        return ((eventEntity, query, cb) -> name != null
                ? cb.like(eventEntity.get("name"), name)
                : null
        );
    }

    public Specification<EventEntity> equalLocationSpec() {
        return ((eventEntity, query, cb) -> locationId != null
                ? cb.equal(eventEntity.get("location.id"), locationId)
                : null
        );
    }

    public Specification<EventEntity> equalStatusSpec() {
        return ((eventEntity, query, cb) -> eventStatus != null
                ? cb.equal(eventEntity.get("status"), eventStatus)
                : null
        );
    }

    public Specification<EventEntity> equalIdSpec() {
        return ((eventEntity, query, cb) -> id != null
                ? cb.equal(eventEntity.get("id"), id)
                : null
        );
    }

    public Specification<EventEntity> equalOwnerSpec() {
        return ((eventEntity, query, cb) -> ownerId != null
                ? cb.equal(eventEntity.get("owner.id"), ownerId)
                : null
        );
    }
}
