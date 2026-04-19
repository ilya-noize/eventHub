package com.event.manager.domain;

import com.event.common.tool.OptionalMapper;
import com.event.manager.api.location.LocationPatchRequest;
import com.event.manager.api.location.LocationPostRequest;
import com.event.manager.api.location.LocationPutRequest;
import com.event.manager.api.location.LocationResponse;
import com.event.manager.db.LocationEntity;
import com.event.manager.domain.location.LocationDto;
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
