package com.event.common.tool;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {
    public static <T> boolean isDifferentValue(T a, T b) {
        return (a != null || b != null) && (a == null || !a.equals(b));
    }

    public static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
