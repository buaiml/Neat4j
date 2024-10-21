package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.util.chance
import kotlin.math.abs

/**
 * This mutation adds a connection between 2 nodes.
 */
class AddConnectionMutation: Mutation() {

    override fun mutate(genome: Genome) {
        // chance to trigger
        if (!neat.random.chance(neat.parameters.mutateAddConnectionChance))
            return

        // Try 50 times to generate a new connection. It might be impossible to
        // add a new connection to this genome.
        var attempts = 50
        while (attempts-- > 0) {

            // Find 2 random nodes
            var a = genome.nodes[neat.random.nextInt(genome.nodes.size)]
            var b = genome.nodes[neat.random.nextInt(genome.nodes.size)]

            // Prevent self-connections, and vertical connections
            val dx = abs(a.position.x() - b.position.x())
            if (a.id == b.id || dx < 0.0001)
                continue

            // Make sure that the connection we create is left->right
            val isFlip = a.position.x() > b.position.x()
            if (isFlip) {
                val temp = a
                a = b
                b = temp
            }

            var connection = neat.getConnectionOrNull(a.id, b.id)

            if (connection != null) {
                // Check if the connection already exists
                if (connection in genome.connections)
                    continue
            } else {
                connection = neat.createConnection(a.id, b.id)
            }

            connection.weight = neat.random.nextGaussian().toFloat()
            genome.connections.add(connection)
            return
        }
    }
}