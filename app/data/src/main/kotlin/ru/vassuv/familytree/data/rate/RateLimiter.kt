package ru.vassuv.familytree.data.rate

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import ru.vassuv.familytree.config.exception.TooManyRequestsException
import java.time.Duration

@Component
class RateLimiter(
    private val redis: StringRedisTemplate,
) {
    /**
     * Ограничивает число событий за окно (в секундах). true — если допущено, false — если превышено.
     * Использует INCR и устанавливает TTL при первом инкременте. Атомарность установки TTL не критична.
     */
    fun allow(key: String, limit: Long, windowSeconds: Long): Boolean {
        if (limit <= 0) return false
        val count = redis.opsForValue().increment(key) ?: 0L
        if (count == 1L) {
            // Установим TTL для окна
            redis.expire(key, Duration.ofSeconds(windowSeconds))
        }
        return count <= limit
    }

    fun ensureAllowedOrThrow(key: String, limit: Long, windowSeconds: Long) {
        if (!allow(key, limit, windowSeconds)) throw TooManyRequestsException()
    }

    /**
     * Дедупликация событий: true — если событие зафиксировано впервые, false — если уже было.
     */
    fun tryAcquireUnique(key: String, ttlSeconds: Long): Boolean {
        return redis.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds)) ?: false
    }
}
