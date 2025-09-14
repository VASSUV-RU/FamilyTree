package ru.vassuv.familytree.service.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.JwtProperties
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64

data class VerifiedToken(
    val subject: String,
    val jti: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val claimsJson: String,
)

@Service
class JwtService(
    private val props: JwtProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val keyPair: KeyPair by lazy { loadOrGenerateKeyPair() }
    private val privateKey: RSAPrivateKey get() = keyPair.private as RSAPrivateKey
    private val publicKey: RSAPublicKey get() = keyPair.public as RSAPublicKey

    private fun b64u(data: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(data)
    private fun b64uDecode(s: String): ByteArray = Base64.getUrlDecoder().decode(s)

    fun sign(subject: String, jti: String, expiresAt: Instant, extraClaims: Map<String, Any?> = emptyMap()): String {
        val now = Instant.now()
        val headerJson = "{" + "\"alg\":\"RS256\",\"typ\":\"JWT\"}" 
        val payloadJson = buildString {
            append('{')
            append("\"sub\":\"").append(escape(subject)).append('\"')
            append(',')
            append("\"jti\":\"").append(escape(jti)).append('\"')
            append(',')
            append("\"iat\":").append(now.epochSecond)
            append(',')
            append("\"exp\":").append(expiresAt.epochSecond)
            append(',')
            append("\"iss\":\"").append(escape(props.issuer)).append('\"')
            // extraClaims only basic support for string/number/boolean
            for ((k, v) in extraClaims) {
                if (v == null) continue
                append(',')
                append('"').append(escape(k)).append('"').append(':')
                when (v) {
                    is Number, is Boolean -> append(v.toString())
                    else -> append('"').append(escape(v.toString())).append('"')
                }
            }
            append('}')
        }
        val header = b64u(headerJson.toByteArray(StandardCharsets.UTF_8))
        val payload = b64u(payloadJson.toByteArray(StandardCharsets.UTF_8))
        val signingInput = "$header.$payload".toByteArray(StandardCharsets.UTF_8)
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initSign(privateKey)
        sig.update(signingInput)
        val signature = b64u(sig.sign())
        return "$header.$payload.$signature"
    }

    fun verifyAndDecode(jwt: String): VerifiedToken {
        val parts = jwt.split('.')
        if (parts.size != 3) throw SecurityException("Malformed JWT")
        val headerB64 = parts[0]
        val payloadB64 = parts[1]
        val sigB64 = parts[2]
        // verify signature
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(publicKey)
        sig.update("$headerB64.$payloadB64".toByteArray(StandardCharsets.UTF_8))
        if (!sig.verify(b64uDecode(sigB64))) throw SecurityException("Invalid signature")

        val payloadJson = String(b64uDecode(payloadB64), StandardCharsets.UTF_8)
        val sub = extractString(payloadJson, "sub") ?: throw SecurityException("sub missing")
        val jti = extractString(payloadJson, "jti") ?: throw SecurityException("jti missing")
        val iat = extractLong(payloadJson, "iat") ?: 0L
        val exp = extractLong(payloadJson, "exp") ?: throw SecurityException("exp missing")
        val now = Instant.now().epochSecond
        if (now > exp) throw SecurityException("Token expired")
        return VerifiedToken(
            subject = sub,
            jti = jti,
            issuedAt = Instant.ofEpochSecond(iat),
            expiresAt = Instant.ofEpochSecond(exp),
            claimsJson = payloadJson,
        )
    }

    private fun escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
    private fun extractString(json: String, key: String): String? {
        val regex = Regex("\"" + Regex.escape(key) + "\"\\s*:\\s*\"([^\"]*)\"")
        return regex.find(json)?.groupValues?.get(1)
    }
    private fun extractLong(json: String, key: String): Long? {
        val regex = Regex("\"" + Regex.escape(key) + "\"\\s*:\\s*([0-9]+)")
        val m = regex.find(json)?.groupValues?.get(1)
        return m?.toLongOrNull()
    }

    private fun loadOrGenerateKeyPair(): KeyPair {
        val pubPem = props.publicKeyPem
        val prvPem = props.privateKeyPem
        return if (!pubPem.isNullOrBlank() && !prvPem.isNullOrBlank()) {
            try {
                KeyPair(loadPublic(pubPem), loadPrivate(prvPem))
            } catch (e: Exception) {
                logger.warn("Failed to read RSA keys from PEM, generating ephemeral: {}", e.message)
                generateEphemeral()
            }
        } else {
            // TODO(ft-auth-05): в проде обязательно передавать PEM ключи через секреты; генерация только для local/dev
            generateEphemeral()
        }
    }

    private fun loadPublic(pem: String): RSAPublicKey {
        val clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()
        val bytes = Base64.getDecoder().decode(clean)
        val kf = KeyFactory.getInstance("RSA")
        val spec = X509EncodedKeySpec(bytes)
        return kf.generatePublic(spec) as RSAPublicKey
    }

    private fun loadPrivate(pem: String): RSAPrivateKey {
        val clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()
        val bytes = Base64.getDecoder().decode(clean)
        val kf = KeyFactory.getInstance("RSA")
        val spec = PKCS8EncodedKeySpec(bytes)
        return kf.generatePrivate(spec) as RSAPrivateKey
    }

    private fun generateEphemeral(): KeyPair {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(2048)
        val kp = gen.generateKeyPair()
        logger.warn("Using ephemeral RSA keypair for JWT signing (dev mode)")
        return kp
    }
}
