package com.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;

import static com.event.common.tool.Utils.getClientIp;

/**
 * Сервис для ограничения частоты запросов на основе Redis.
 * Ограничение реализовано через счетчики в редисе.
 * <br>5 попыток за одну минуту.
 */
@Service
@Slf4j
public class RateLimitService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration EXPIRY = Duration.ofMinutes(1);
    private final ValueOperations<String, String> redisOps;

    public RateLimitService(StringRedisTemplate template) {
        this.redisOps = template.opsForValue();
    }

    public void validateFiveRequestsOneMinute(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String rateLimitKey = "login:" + clientIp;

        if (!allowRequest(rateLimitKey)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Проверяет, разрешён ли запрос по ключу (например, IP).
     * Использует инкремент с TTL.
     *
     * @param key уникальный ключ (например, "login:192.168.1.1")
     * @return true, если лимит не превышен
     */
    private boolean allowRequest(String key) {
        String value = redisOps.get(key);
        if (value == null) {
            // Первый запрос — создаём счётчик
            redisOps.set(key, "1", EXPIRY);
            return true;
        }

        long current = Long.parseLong(value);
        if (current >= MAX_ATTEMPTS) {
            return false; // Лимит превышен
        }

        // Инкремент счётчика
        redisOps.increment(key);
        return true;
    }
}

