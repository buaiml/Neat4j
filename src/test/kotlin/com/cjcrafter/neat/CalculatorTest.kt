package com.cjcrafter.neat

import com.cjcrafter.neat.compute.SimpleCalculator
import com.cjcrafter.neat.genome.ConnectionGene
import org.joml.Vector2f
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CalculatorTest {

    @Test
    fun testEmptyNetwork() {
        val parameters = Parameters()
        parameters.useBiasNode = false
        val neat = NeatImpl(3, 1, 1, parameters)

        val genome = neat.createGenome(true)
        val calculator = SimpleCalculator(genome)
        val output = calculator.calculate(floatArrayOf(1.0f, 0.0f, 1.0f)).join()
        assertEquals(1, output.size)
        assertEquals(0.5f, output[0])
    }

    @Test
    fun testSimpleNetwork() {
        val parameters = Parameters()
        parameters.useBiasNode = false
        val neat = NeatImpl(3, 1, 1, parameters)

        val genome = neat.createGenome(true)
        genome.connections.add(ConnectionGene(neat, 0, 0, 3).apply { weight = 1f })
        genome.connections.add(ConnectionGene(neat, 1, 1, 3).apply { weight = 1f })
        genome.connections.add(ConnectionGene(neat, 2, 2, 3).apply { weight = 1f })
        val calculator = SimpleCalculator(genome)
        val output = calculator.calculate(floatArrayOf(1.0f, 0.0f, 1.0f)).join()

        val expected = 1f / (1f + Math.exp(-4.9 * 2)).toFloat()
        assertEquals(1, output.size)
        assertEquals(expected, output[0])
    }

    @Test
    fun testXorNetwork() {
        val parameters = Parameters()
        parameters.useBiasNode = false
        val neat = NeatImpl(3, 1, 1, parameters)

        val genome = neat.createGenome(true)
        val newNode = neat.createNode()
        newNode.position = Vector2f(0.5f, 0.5f)
        genome.nodes.add(newNode)
        genome.connections.add(ConnectionGene(neat, 0, 0, newNode.id).apply { weight = -0.8f })
        genome.connections.add(ConnectionGene(neat, 1, 1, newNode.id).apply { weight = 0.7f })
        genome.connections.add(ConnectionGene(neat, 2, 2, newNode.id).apply { weight = 0.7f })
        genome.connections.add(ConnectionGene(neat, 3, newNode.id, 3).apply { weight = -1.5f })
        genome.connections.add(ConnectionGene(neat, 4, 1, 3).apply { weight = 0.6f })
        genome.connections.add(ConnectionGene(neat, 5, 2, 3).apply { weight = 0.6f })

        val calculator = SimpleCalculator(genome)

        val xor = arrayOf(
            Pair(floatArrayOf(1f, 0f, 0f), 0f),
            Pair(floatArrayOf(1f, 0f, 1f), 1f),
            Pair(floatArrayOf(1f, 1f, 0f), 1f),
            Pair(floatArrayOf(1f, 1f, 1f), 0f)
        )

        for ((input, expected) in xor) {
            println("Testing ${input[1].toInt()} XOR ${input[2].toInt()} = $expected")
            val output = calculator.calculate(input).join()
            assertEquals(1, output.size)

            val normalized = if (output[0] > 0.5f) 1f else 0f
            assertEquals(expected, normalized)
            println("Passed!")
        }
    }
}