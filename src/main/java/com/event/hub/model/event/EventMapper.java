package com.event.hub.model.event;

import com.event.hub.db.entity.EventEntity;
import com.event.hub.model.location.LocationMapper;
import com.event.hub.model.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, LocationMapper.class}
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

    @Mapping(target = "ownerId", source = "entity.owner.id")
    @Mapping(target = "locationId", source = "entity.location.id")
    EventDto toDomain(EventEntity entity);

    EventResponse toResponse(EventDto domain);

    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "location.id", source = "eventDto.locationId")
    @Mapping(target = "owner.id", source = "eventDto.ownerId")
    EventEntity toEntity(EventDto eventDto);
}
