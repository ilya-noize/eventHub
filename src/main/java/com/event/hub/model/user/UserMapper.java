package com.event.hub.model.user;

import com.event.hub.db.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "events", ignore = true)
    UserEntity toEntity(User domain);

    User toDomain(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toDomain(UserRegistration registration);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toDomain(UserCredentials credentials);

    UserResponse toResponse(User user);
}
