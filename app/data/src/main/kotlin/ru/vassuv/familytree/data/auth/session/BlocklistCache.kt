package ru.vassuv.familytree.data.auth.session

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BlocklistCache(
    private val redis: StringRedisTemplate,
) {
    private fun key(jti: String) = "blk:$jti"

    fun block(jti: String, ttlSeconds: Long, reason: String? = null) {
        val v = reason ?: "1"
        redis.opsForValue().set(key(jti), v, Duration.ofSeconds(ttlSeconds))
    }

    fun isBlocked(jti: String): Boolean = redis.opsForValue().get(key(jti)) != null
}

