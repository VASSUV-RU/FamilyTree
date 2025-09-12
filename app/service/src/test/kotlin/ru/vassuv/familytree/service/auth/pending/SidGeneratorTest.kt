package ru.vassuv.familytree.service.auth.pending

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.vassuv.familytree.service.auth.pending.SidGenerator

class SidGeneratorTest {
    @Test
    fun `generates opaque short sid`() {
        val gen = SidGenerator()
        val s1 = gen.generate()
        val s2 = gen.generate()

        // Starts with prefix and within deep-link payload limit
        assertTrue(s1.startsWith("S"))
        assertTrue(s1.length <= 64)

        // Randomness
        assertNotEquals(s1, s2)
    }
}

