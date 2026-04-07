package com.event.hub.model.location;

import com.event.hub.db.entity.LocationEntity;
import com.event.hub.model.OptionalMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        uses = {OptionalMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface LocationMapper {

    @Mapping(target = "events", ignore = true)
    LocationEntity toEntity(LocationDto locationDto);

    @Mapping(target = "id", ignore = true)
    LocationDto toDomain(LocationPostRequest request);

    @Mapping(target = "id", ignore = true)
    LocationDto toDomain(LocationPutRequest request);

    LocationDto toDomain(LocationPatchRequest request);

    LocationDto toDomain(LocationEntity request);

    LocationResponse toResponse(LocationDto locationDto);
}
