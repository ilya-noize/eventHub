package com.event.notifier.db;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisNotificationsCounter {
    private static final Duration EXPIRY = Duration.ofDays(7);
    private static final String CACHE_PREFIX_SUBSCRIBER = "subscriber::";
    private final ValueOperations<String, String> redisOps;

    public void increment(Long id) {
        String key = getKey(id);
        String value = redisOps.get(key);
        if(value == null) {
            redisOps.set(key, "1", EXPIRY);
        } else {
            redisOps.increment(key);
        }
    }

    public void decrement(Long userId, Long affectedRowsCount) {
        String key = getKey(userId);
        String value = redisOps.get(key);
        if(value == null) {
            redisOps.set(key, "0", EXPIRY);
        } else {
            redisOps.decrement(key, affectedRowsCount);
        }
    }

    private static String getKey(Long userId) {
        return CACHE_PREFIX_SUBSCRIBER.concat("%s").formatted(userId);
    }
}
