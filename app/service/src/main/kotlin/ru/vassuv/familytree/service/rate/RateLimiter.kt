package ru.vassuv.familytree.service.rate

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import ru.vassuv.familytree.config.exception.TooManyRequestsException

@Component
class RateLimiter(
    private val redis: StringRedisTemplate,
) {
    /**
     * Лимитирует число событий за окно в секундах. Возвращает true если допущено, false если превышено.
     */
    fun allow(key: String, limit: Long, windowSeconds: Long): Boolean {
        if (limit <= 0) return false
        val count = redis.execute { conn ->
            val ser = redis.stringSerializer
            val k = ser.serialize(key)!!
            val v = conn.numberCommands().incr(k)
            if (v == 1L) {
                conn.keyCommands().expire(k, windowSeconds)
            }
            v
        } ?: 0L
        return count <= limit
    }

    fun ensureAllowedOrThrow(key: String, limit: Long, windowSeconds: Long) {
        if (!allow(key, limit, windowSeconds)) throw TooManyRequestsException()
    }

    /**
     * Дедупликация событий: возвращает true, если успешно зафиксировали событие как уникальное,
     * false — если уже было (ключ существовал). TTL задаёт окно дедупликации.
     */
    fun tryAcquireUnique(key: String, ttlSeconds: Long): Boolean {
        return redis.execute { conn ->
            val ser = redis.stringSerializer
            val k = ser.serialize(key)!!
            val ok = conn.stringCommands().setNX(k, ser.serialize("1")!!)
            if (ok == true) conn.keyCommands().expire(k, ttlSeconds)
            ok == true
        } ?: false
    }
}

