package ru.vassuv.familytree.data.auth.pending

import com.redis.testcontainers.RedisContainer
import org.junit.jupiter.api.*
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@Testcontainers
class PendingSessionRepositoryRedisTest {

    companion object {
        @Container
        @JvmStatic
        val redisC: RedisContainer = RedisContainer(DockerImageName.parse("redis:7.2-alpine"))
    }

    private lateinit var template: StringRedisTemplate
    private lateinit var repo: PendingSessionRepositoryRedis

    @BeforeEach
    fun setUp() {
        val factory = LettuceConnectionFactory(redisC.host, redisC.firstMappedPort)
        factory.afterPropertiesSet()
        template = StringRedisTemplate(factory)
        template.afterPropertiesSet()
        repo = PendingSessionRepositoryRedis(template)
    }

    private fun newSid(): String = "S" + UUID.randomUUID().toString().replace("-", "")

    @Test
    fun `create and get pending session`() {
        val sid = newSid()
        val ok = repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING, invitationId = "inv-1"), 5)
        assertTrue(ok)

        val loaded = repo.get(sid)
        assertNotNull(loaded)
        assertEquals(PendingSessionStatus.PENDING, loaded!!.status)
        assertEquals("inv-1", loaded.invitationId)
    }

    @Test
    fun `create same sid returns false`() {
        val sid = "Sfixed"
        assertTrue(repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING), 5))
        assertFalse(repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING), 5))
    }

    @Test
    fun `ttl expiration removes session`() {
        val sid = newSid()
        assertTrue(repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING), 1))
        TimeUnit.SECONDS.sleep(2)
        val loaded = repo.get(sid)
        assertNull(loaded)
        val res = repo.markReady(sid, loaded?.telegramId ?: 0L)
        assertEquals(MarkReadyResult.NotFound, res)
    }

    @Test
    fun `markReady idempotent and markUsed flow`() {
        val sid = newSid()
        repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING), 10)
        val r1 = repo.markReady(sid, telegramId = 1001)
        assertEquals(MarkReadyResult.Ok, r1)

        // Second ready should be AlreadyReady
        val r2 = repo.markReady(sid, telegramId = 1001)
        assertEquals(MarkReadyResult.AlreadyReady, r2)

        // Now mark used
        val u1 = repo.markUsed(sid)
        assertEquals(MarkUsedResult.Ok, u1)

        // Mark ready after used → AlreadyUsed
        val r3 = repo.markReady(sid, telegramId = 1001)
        assertEquals(MarkReadyResult.AlreadyUsed, r3)

        // Mark used again is Ok (idempotent)
        val u2 = repo.markUsed(sid)
        assertEquals(MarkUsedResult.Ok, u2)
    }

    @Test
    fun `concurrent markReady yields conflict for one`() {
        val sid = newSid()
        repo.create(sid, PendingSessionRecord(sid, PendingSessionStatus.PENDING), 10)

        val pool = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(1)
        var res1: MarkReadyResult? = null
        var res2: MarkReadyResult? = null

        pool.submit {
            latch.await()
            res1 = repo.markReady(sid, telegramId = 1001)
        }
        pool.submit {
            latch.countDown()
            res2 = repo.markReady(sid, telegramId = 1002)
        }
        pool.shutdown()
        pool.awaitTermination(5, TimeUnit.SECONDS)

        val a = res1!!
        val b = res2!!
        assertTrue((a == MarkReadyResult.Ok && (b == MarkReadyResult.Conflict || b == MarkReadyResult.AlreadyReady)) ||
                   (b == MarkReadyResult.Ok && (a == MarkReadyResult.Conflict || a == MarkReadyResult.AlreadyReady)))
    }

    @Test
    fun `markUsed on missing sid returns NotFound`() {
        val res = repo.markUsed("S_not_exist")
        assertEquals(MarkUsedResult.NotFound, res)
    }
}
