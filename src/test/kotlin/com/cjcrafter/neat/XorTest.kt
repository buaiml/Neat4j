package com.cjcrafter.neat

import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals

class XorTest {

    @Test
    fun textXor() {
        // Create a new NEAT instance with 3 input nodes, 1 output node, and 100 clients
        // The first input node is the bias node, which is always 1. The other 2 input nodes
        // are the XOR inputs (either 0 or 1). The output node is the XOR result.
        val neat: Neat = NeatImpl(3, 1, 100)
        val debug = NeatPrinter(neat)
        debug.print()

        val xor = listOf(
            Pair(floatArrayOf(1f, 0f, 0f), floatArrayOf(0f)),
            Pair(floatArrayOf(1f, 0f, 1f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 0f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 1f), floatArrayOf(0f)),
        )

        val steps = 100
        for (i in 0 until steps) {

            // Calculate the score for each client
            for (client in neat.clients) {
                client.score = 4.0
                for (pair in xor) {
                    val input = pair.first
                    val output = pair.second

                    val result = client.calculator.calculate(input).join()

                    // Calculate the score
                    val diff = abs(result[0] - output[0])
                    client.score -= diff * diff
                }
            }

            if (i != steps - 1) {
                // Do a generation step
                neat.evolve()
            }
            debug.print()
        }

        // The best client should have a working XOR network
        val bestClient = neat.clients.maxByOrNull { it.score }!!
        println("Best client: ${bestClient.score}")
        for (pair in xor) {
            val input = pair.first
            val output = pair.second

            val result = bestClient.calculator.calculate(input).join()
            val normalized = if (result[0] > 0.5f) 1f else 0f
            assertEquals(output[0], normalized, "${input[1]} XOR ${input[2]} = $normalized (expected ${output[0]}).")
        }
    }

    @Test
    fun testUntilTrue() {
        val neat = NeatImpl(3, 1, 150)
        val debug = NeatPrinter(neat)
        debug.print()

        val xor = listOf(
            Pair(floatArrayOf(1f, 0f, 0f), floatArrayOf(0f)),
            Pair(floatArrayOf(1f, 0f, 1f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 0f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 1f), floatArrayOf(0f)),
        )

        var generations = 0
        while (true)  {
            for (client in neat.clients) {
                client.score = 4.0
                for (pair in xor) {
                    val input = pair.first
                    val output = pair.second

                    val result = client.calculator.calculate(input).join()

                    // Calculate the score
                    val diff = abs(result[0] - output[0])
                    client.score -= diff
                }

                // Square the score to make it more important
                client.score *= client.score
            }

            if (passes(neat.clients.maxByOrNull { it.score }!!, xor)) {
                break
            }

            neat.evolve()
            generations++
            println(generations)

            if (generations % 10 == 0) {
                debug.print()
            }
        }
    }

    private fun passes(client: Client, xor: List<Pair<FloatArray, FloatArray>>): Boolean {
        for (pair in xor) {
            val input = pair.first
            val output = pair.second

            val result = client.calculator.calculate(input).join()
            val normalized = if (result[0] > 0.5f) 1f else 0f
            if (output[0] != normalized) {
                return false
            }
        }
        return true
    }
}