package ru.vassuv.familytree.data.auth.pending

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.types.Expiration
import org.springframework.stereotype.Repository
import java.time.Instant
import org.springframework.data.redis.connection.RedisStringCommands

@Repository
class PendingSessionRepositoryRedis(
    private val redis: StringRedisTemplate,
) : PendingSessionRepository {

    private val mapper = jacksonObjectMapper()

    private fun keyData(sid: String) = "tg:pending:$sid"
    private fun keyStatus(sid: String) = "tg:pending:$sid:status"

    override fun create(sid: String, record: PendingSessionRecord, ttlSeconds: Long): Boolean {
        val dataKey = keyData(sid)
        val statusKey = keyStatus(sid)
        val json = mapper.writeValueAsString(record.copy(status = PendingSessionStatus.pending))
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val sk = ser.serialize(statusKey)!!
            val dk = ser.serialize(dataKey)!!
            val pending = ser.serialize(PendingSessionStatus.pending.name)!!
            val ok = connection.stringCommands().set(sk, pending, Expiration.seconds(ttlSeconds), RedisStringCommands.SetOption.ifAbsent())
            if (ok == true) {
                connection.stringCommands().set(dk, ser.serialize(json)!!)
                connection.keyCommands().expire(sk, ttlSeconds)
                connection.keyCommands().expire(dk, ttlSeconds)
                true
            } else false
        } ?: false
    }

    override fun get(sid: String): PendingSessionRecord? {
        val dataKey = keyData(sid)
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val dk = ser.serialize(dataKey)!!
            val bytes = connection.stringCommands().get(dk) ?: return@execute null
            val json = ser.deserialize(bytes) ?: return@execute null
            try { mapper.readValue<PendingSessionRecord>(json) } catch (_: Exception) { null }
        }
    }

    override fun markReady(sid: String, authPayload: Map<String, Any?>, userId: String?): MarkReadyResult {
        val statusKey = keyStatus(sid)
        val dataKey = keyData(sid)
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val sk = ser.serialize(statusKey)!!
            val dk = ser.serialize(dataKey)!!

            connection.watch(sk)
            val statusBytes = connection.stringCommands().get(sk) ?: return@execute MarkReadyResult.NotFound
            val status = ser.deserialize(statusBytes)
            if (status == PendingSessionStatus.ready.name) return@execute MarkReadyResult.AlreadyReady
            if (status == PendingSessionStatus.used.name) return@execute MarkReadyResult.AlreadyUsed

            val current = connection.stringCommands().get(dk)
            val rec = try {
                val json = current?.let { ser.deserialize(it) }
                if (json != null) mapper.readValue<PendingSessionRecord>(json) else PendingSessionRecord(sid = sid, status = PendingSessionStatus.pending, createdAt = Instant.now().epochSecond)
            } catch (_: Exception) {
                PendingSessionRecord(sid = sid, status = PendingSessionStatus.pending, createdAt = Instant.now().epochSecond)
            }
            val updated = rec.copy(status = PendingSessionStatus.ready, userId = userId ?: rec.userId, auth = authPayload)

            connection.multi()
            connection.stringCommands().set(sk, ser.serialize(PendingSessionStatus.ready.name)!!)
            connection.stringCommands().set(dk, ser.serialize(mapper.writeValueAsString(updated))!!)
            val res = connection.exec()
            if (res == null) MarkReadyResult.Conflict else MarkReadyResult.Ok
        } ?: MarkReadyResult.Conflict
    }

    override fun markUsed(sid: String): MarkUsedResult {
        val statusKey = keyStatus(sid)
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val sk = ser.serialize(statusKey)!!

            connection.watch(sk)
            val statusBytes = connection.stringCommands().get(sk) ?: return@execute MarkUsedResult.NotFound
            val status = ser.deserialize(statusBytes)
            if (status == PendingSessionStatus.used.name) return@execute MarkUsedResult.Ok

            connection.multi()
            connection.stringCommands().set(sk, ser.serialize(PendingSessionStatus.used.name)!!)
            val res = connection.exec()
            if (res == null) MarkUsedResult.Conflict else MarkUsedResult.Ok
        } ?: MarkUsedResult.Conflict
    }
}

