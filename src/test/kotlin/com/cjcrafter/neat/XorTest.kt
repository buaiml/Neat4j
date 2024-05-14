package com.cjcrafter.neat

import org.junit.jupiter.api.Test

class XorTest {

    @Test
    fun testXor() {
        val neat = XorUtil.createNeat()
        val debug = NeatPrinter(neat)
        var generation = 0

        while (!XorUtil.score(neat) && generation < 100) {
            generation++

            println(debug.render())
            neat.evolve()
        }

        println("Generations: $generation")
    }
}