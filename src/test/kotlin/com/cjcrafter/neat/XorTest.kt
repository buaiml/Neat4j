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

        val xor = listOf(
            Pair(floatArrayOf(1f, 0f, 0f), floatArrayOf(0f)),
            Pair(floatArrayOf(1f, 0f, 1f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 0f), floatArrayOf(1f)),
            Pair(floatArrayOf(1f, 1f, 1f), floatArrayOf(0f)),
        )

        val steps = 100
        val averageScores = FloatArray(steps)
        val highestScores = FloatArray(steps)
        for (i in 0 until steps) {

            var scoreSum = 0f

            // Calculate the score for each client
            val clients = neat.clients
            for (client in clients) {
                var score = 0.0
                for (pair in xor) {
                    val input = pair.first
                    val output = pair.second

                    val result = client.calculator.calculate(input).join()
                    // Normalize the outputs, since we expect either 0 or 1
                    // result[0] = if (result[0] > 0.5f) 1f else 0f

                    // Calculate the score
                    score += 1 - abs(result[0] - output[0])
                }

                client.score = score / xor.size
                scoreSum += score.toFloat() / xor.size

                highestScores[i] = maxOf(highestScores[i], client.score.toFloat())
            }

            // Calculate the average score
            averageScores[i] = scoreSum / clients.size.toFloat()

            // Do a generation step
            neat.evolve()
        }

        // Print the average scores
        for (i in 0 until steps) {
            println("Step $i: ${averageScores[i]} : ${highestScores[i]}")
        }

        // The best client should have a working XOR network
        val bestClient = neat.clients.maxByOrNull { it.score }!!
        println("Best client: ${bestClient.score}")
        for (pair in xor) {
            val input = pair.first
            val output = pair.second

            val result = bestClient.calculator.calculate(input).join()
            val normalized = if (result[0] > 0.5f) 1f else 0f
            assertEquals(output[0], normalized)
        }
    }
}