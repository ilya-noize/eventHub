package com.event.common.tool;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class OptionalMapper {
    public static <T> T map(Optional<T> optional) {
        return optional.orElse(null);
    }
}
