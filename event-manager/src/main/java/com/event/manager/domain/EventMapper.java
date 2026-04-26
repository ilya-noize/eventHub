package com.event.manager.domain;

import com.event.manager.api.event.EventPostRequest;
import com.event.manager.api.event.EventPutRequest;
import com.event.manager.api.event.EventResponse;
import com.event.manager.db.EventEntity;
import com.event.manager.domain.event.EventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {LocationMapper.class}
)
public interface EventMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "occupiedPlaces", ignore = true)
    @Mapping(target = "id", ignore = true)
    EventDto toDomain(EventPostRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "occupiedPlaces", ignore = true)
    @Mapping(target = "id", ignore = true)
    EventDto toDomain(EventPutRequest request);

    @Mapping(target = "ownerId", source = "entity.ownerId")
    @Mapping(target = "locationId", source = "entity.location.id")
    EventDto toDomain(EventEntity entity);

    EventResponse toResponse(EventDto domain);

    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "location.id", source = "eventDto.locationId")
    @Mapping(target = "ownerId", source = "eventDto.ownerId")
    EventEntity toEntity(EventDto eventDto);
}
