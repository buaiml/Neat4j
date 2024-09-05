package com.cjcrafter.neat

import org.junit.jupiter.api.Test

class CrossoverTest {

    @Test
    fun testCrossover() {
        // create a dummy Neat instance
        val parameters = Parameters()
        parameters.useBiasNode = false
        val neat = NeatImpl(2, 1, 100, parameters)

        // create 2 genomes to crossover
        val genome1 = neat.createGenome(true)
        val genome2 = neat.createGenome(true)

        // create a connection between 2 nodes
        val connection = neat.createConnection(0, 1)
        connection.weight = 0.5f
        genome1.connections.add(connection)

        val crossover = genome1 % genome2

        // This crossover should match up perfectly with genome1
        assert(crossover.connections.size == 1)
        assert(crossover.nodes.size == 3)
        assert(crossover.connections[0].weight == 0.5f)
    }
}