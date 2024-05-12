package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.ThreadLocalRandom

/**
 * This mutation adds a connection between 2 nodes.
 *
 * @property neat The [Neat] instance managing this object.
 */
class AddConnectionMutation(override val neat: Neat): Mutation {
    override fun mutate(genome: Genome) {
        // chance to trigger
        val rand = ThreadLocalRandom.current()
        if (rand.nextFloat() >= neat.parameters.mutationAddConnectionChance)
            return

        // Try 50 times to generate a new connection. It might be impossible to
        // add a new connection to this genome.
        var attempts = 50
        while (attempts-- > 0) {

            // Find 2 random nodes
            var a = genome.nodes[rand.nextInt(genome.nodes.size)]
            var b = genome.nodes[rand.nextInt(genome.nodes.size)]

            // Prevent self-connections, and vertical connections
            if (a.id == b.id || a.position.x() == b.position.x())
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

            connection.weight = rand.nextGaussian().toFloat()
            genome.connections.add(connection)
            return
        }
    }
}