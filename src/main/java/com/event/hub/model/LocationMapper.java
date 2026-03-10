package com.event.hub.model;

import com.event.hub.db.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        uses = {OptionalMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface LocationMapper {

    LocationEntity toEntity(Location location);

    @Mapping(target = "id", ignore = true)
    Location toDomain(LocationPostRequest request);

    Location toDomain(LocationPutRequest request);

    Location toDomain(LocationPatchRequest request);

    Location toDomain(LocationEntity request);

    LocationResponse toResponse(Location location);
}
