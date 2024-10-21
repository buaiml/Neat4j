package com.cjcrafter.neat

import com.cjcrafter.neat.util.ProbabilityMap
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.math.sqrt
import kotlin.test.assertEquals

class ProbabilityTest {

    @Test
    fun testProbabilityMap() {
        val attempts = 100_000
        val map = ProbabilityMap<Int>(SplittableRandom(0))
        map[1] = 0.5
        map[2] = 0.25
        map[3] = 0.25

        val counts = mutableMapOf<Int, Int>()
        for (i in 0 until attempts) {
            val value = map.get()
            counts[value] = counts.getOrDefault(value, 0) + 1
        }

        val expectedCounts = mapOf(1 to 0.5 * attempts, 2 to 0.25 * attempts, 3 to 0.25 * attempts)
        expectedCounts.forEach { (key, expectedCount) ->
            val actualCount = counts[key]!!.toDouble()
            val sigma = sqrt(attempts * (expectedCount / attempts) * (1 - (expectedCount / attempts)))
            val tolerance = 3 * sigma // 3σ tolerance
            assertEquals(expectedCount, actualCount, tolerance)
        }
    }
}