package com.event.common.tool;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtTokenUtils {
    private static final String PREFIX ="Bearer ";

    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(PREFIX)) {
            return authHeader.substring(PREFIX.length());
        }
        return null;
    }
}
