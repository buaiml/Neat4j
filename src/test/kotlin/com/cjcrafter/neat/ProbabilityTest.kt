package com.cjcrafter.neat

import com.cjcrafter.neat.util.ProbabilityMap
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class ProbabilityTest {

    @Test
    fun testProbabilityMap() {
        val map = ProbabilityMap<Int>(SplittableRandom())
        map[1] = 0.5
        map[2] = 0.25
        map[3] = 0.25

        val counts = mutableMapOf<Int, Int>()
        val attempts = 5000
        for (i in 0 until attempts) {
            val value = map.get()
            counts[value] = counts.getOrDefault(value, 0) + 1
        }

        // The Expected values are 500, 250, and 250. The distribution is uniform,
        // so the actual values should be close to the expected values. We allow a
        // 5% error margin.
        assertEquals(0.5 * attempts, counts[1]!!.toDouble(), 0.5 * attempts * 0.05)
        assertEquals(0.25 * attempts, counts[2]!!.toDouble(), 0.25 * attempts * 0.05)
        assertEquals(0.25 * attempts, counts[3]!!.toDouble(), 0.25 * attempts * 0.05)

        // Print results!
        println("1: ${counts[1]!! / attempts.toDouble()}")
        println("2: ${counts[2]!! / attempts.toDouble()}")
        println("3: ${counts[3]!! / attempts.toDouble()}")
    }
}