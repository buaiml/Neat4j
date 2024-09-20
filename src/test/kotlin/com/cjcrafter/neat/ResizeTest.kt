package com.cjcrafter.neat

import org.junit.jupiter.api.Test
import java.util.concurrent.ThreadLocalRandom
import kotlin.test.assertEquals

class ResizeTest {

    @Test
    fun test_addClients() {
        val parameters = Parameters(
            useBiasNode = false,
        )
        val neat = NeatImpl(2, 1, 100, parameters)
        assertEquals(100, neat.clients.size)

        neat.updateClients(150)
        assertEquals(150, neat.clients.size)

        // When we add a new input node, we expect the genomes to have the new input node
        for (client in neat.clients) {
            assertEquals(2, client.genome.nodes.count { it.isInput() })
            assertEquals(1, client.genome.nodes.count { it.isOutput() })
            assertEquals(0, client.genome.nodes.count { it.isHidden() })
        }
    }

    @Test
    fun test_removeClients() {
        val parameters = Parameters(
            useBiasNode = false,
        )
        val neat = NeatImpl(2, 1, 100, parameters)
        assertEquals(100, neat.clients.size)

        neat.updateClients(50)
        assertEquals(50, neat.clients.size)

        // When we remove a client, we expect the genomes to have the input node removed
        for (client in neat.clients) {
            assertEquals(2, client.genome.nodes.count { it.isInput() })
            assertEquals(1, client.genome.nodes.count { it.isOutput() })
            assertEquals(0, client.genome.nodes.count { it.isHidden() })
        }
    }

    @Test
    fun test_addInputNodeToEmptyClients() {
        val parameters = Parameters(
            useBiasNode = false,
        )
        val neat = NeatImpl(2, 1, 100, parameters)
        assertEquals(2, neat.countInputNodes)

        neat.updateNodeCounts(3, 1)
        assertEquals(3, neat.countInputNodes)

        // When we have 3 input nodes, we expect the genomes to have 3 input nodes
        for (client in neat.clients) {
            assertEquals(3, client.genome.nodes.count { it.isInput() })
            assertEquals(1, client.genome.nodes.count { it.isOutput() })
            assertEquals(0, client.genome.nodes.count { it.isHidden() })
        }
    }

    @Test
    fun test_addOutputNodeToEmptyClients() {
        val parameters = Parameters(
            useBiasNode = false,
        )
        val neat = NeatImpl(2, 1, 100, parameters)
        assertEquals(1, neat.countOutputNodes)

        neat.updateNodeCounts(2, 2)
        assertEquals(2, neat.countOutputNodes)

        // When we have 2 output nodes, we expect the genomes to have 2 output nodes
        for (client in neat.clients) {
            assertEquals(2, client.genome.nodes.count { it.isOutput() })
            assertEquals(2, client.genome.nodes.count { it.isInput() })
            assertEquals(0, client.genome.nodes.count { it.isHidden() })
        }
    }

    @Test
    fun test_changeNodesRandomlyWithMutations() {
        val attempts = 100
        val parameters = Parameters(
            useBiasNode = false,
        )

        for (i in 0 until attempts) {
            println("Testing attempt $i of $attempts")
            val neat = NeatImpl(2, 1, 100, parameters)

            // Evolve the networks, so we get complicated networks before
            // making any changes
            val generations = 100
            for (j in 0 until generations) {
                print("$j, ")
                neat.evolve()
            }
            println()

            val newInputNodes = neat.countInputNodes + ThreadLocalRandom.current().nextInt(5)
            val newOutputNodes = neat.countOutputNodes + ThreadLocalRandom.current().nextInt(5)
            neat.updateNodeCounts(newInputNodes, newOutputNodes)

            // When we have new input and output nodes, we expect the genomes to have them
            for (client in neat.clients) {
                assertEquals(newInputNodes, client.genome.nodes.count { it.isInput() })
                assertEquals(newOutputNodes, client.genome.nodes.count { it.isOutput() })
                // assertEquals(0, client.genome.nodes.count { it.isHidden() })
            }
        }
    }
}
