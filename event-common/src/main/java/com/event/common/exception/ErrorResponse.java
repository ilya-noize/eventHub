package com.event.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ErrorResponse(
        String message,
        String detailedMessage,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd HH:mm:ss.SSS'Z'"
        )
        OffsetDateTime dateTime
) {
}
