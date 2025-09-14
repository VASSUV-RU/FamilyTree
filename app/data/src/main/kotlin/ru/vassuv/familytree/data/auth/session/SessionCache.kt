package ru.vassuv.familytree.data.auth.session

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

data class CachedSession(
    val jti: String,
    val userId: Long,
    val activeFamilyId: Long? = null,
    val scopes: String? = null,
    val capVersion: Int = 1,
)

@Component
class SessionCache(
    private val redis: StringRedisTemplate,
) {
    private val mapper = jacksonObjectMapper()
    private fun key(jti: String) = "sess:$jti"

    fun put(session: CachedSession, ttlSeconds: Long) {
        val k = key(session.jti)
        val json = mapper.writeValueAsString(session)
        redis.opsForValue().set(k, json, Duration.ofSeconds(ttlSeconds))
    }

    fun get(jti: String): CachedSession? {
        val v = redis.opsForValue().get(key(jti)) ?: return null
        return runCatching { mapper.readValue<CachedSession>(v) }.getOrNull()
    }

    fun evict(jti: String) {
        redis.delete(key(jti))
    }
}

