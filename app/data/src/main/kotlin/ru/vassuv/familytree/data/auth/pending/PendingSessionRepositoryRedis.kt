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
    private fun keyKnown(sid: String) = "tg:pending:$sid:known"

    override fun create(sid: String, record: PendingSessionRecord, ttlSeconds: Long): Boolean {
        val dataKey = keyData(sid)
        val statusKey = keyStatus(sid)
        val knownKey = keyKnown(sid)
        val json = mapper.writeValueAsString(record.copy(status = PendingSessionStatus.PENDING))
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val sk = ser.serialize(statusKey)!!
            val dk = ser.serialize(dataKey)!!
            val kk = ser.serialize(knownKey)!!
            val pending = ser.serialize(PendingSessionStatus.PENDING.name)!!
            val ok = connection.stringCommands().set(sk, pending, Expiration.seconds(ttlSeconds), RedisStringCommands.SetOption.ifAbsent())
            if (ok == true) {
                connection.stringCommands().set(dk, ser.serialize(json)!!)
                connection.keyCommands().expire(sk, ttlSeconds)
                connection.keyCommands().expire(dk, ttlSeconds)
                // mark sid as known a bit longer than TTL to distinguish expired vs never existed
                connection.stringCommands().set(kk, ser.serialize("1")!!)
                connection.keyCommands().expire(kk, ttlSeconds + 300)
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
            if (status == PendingSessionStatus.READY.name) return@execute MarkReadyResult.AlreadyReady
            if (status == PendingSessionStatus.USED.name) return@execute MarkReadyResult.AlreadyUsed

            val current = connection.stringCommands().get(dk)
            val rec = try {
                val json = current?.let { ser.deserialize(it) }
                if (json != null) mapper.readValue<PendingSessionRecord>(json) else PendingSessionRecord(sid = sid, status = PendingSessionStatus.PENDING, createdAt = Instant.now().epochSecond)
            } catch (_: Exception) {
                PendingSessionRecord(sid = sid, status = PendingSessionStatus.PENDING, createdAt = Instant.now().epochSecond)
            }
            val updated = rec.copy(status = PendingSessionStatus.READY, userId = userId ?: rec.userId, auth = authPayload)

            connection.multi()
            connection.stringCommands().set(sk, ser.serialize(PendingSessionStatus.READY.name)!!)
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
            if (status == PendingSessionStatus.USED.name) return@execute MarkUsedResult.Ok

            connection.multi()
            connection.stringCommands().set(sk, ser.serialize(PendingSessionStatus.USED.name)!!)
            val res = connection.exec()
            if (res == null) MarkUsedResult.Conflict else MarkUsedResult.Ok
        } ?: MarkUsedResult.Conflict
    }

    override fun isKnown(sid: String): Boolean {
        val knownKey = keyKnown(sid)
        return redis.execute { connection ->
            val ser = redis.stringSerializer
            val kk = ser.serialize(knownKey)!!
            val bytes = connection.stringCommands().get(kk)
            bytes != null
        } ?: false
    }
}
