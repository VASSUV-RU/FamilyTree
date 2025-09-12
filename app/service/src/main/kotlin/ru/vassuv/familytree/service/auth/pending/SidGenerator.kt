package ru.vassuv.familytree.service.auth.pending

import java.security.SecureRandom
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class SidGenerator(
    private val random: SecureRandom = SecureRandom()
) {
    fun generate(byteLength: Int = 16, prefix: String = "S"): String {
        val bytes = ByteArray(byteLength)
        random.nextBytes(bytes)
        val base = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return prefix + base
    }
}

