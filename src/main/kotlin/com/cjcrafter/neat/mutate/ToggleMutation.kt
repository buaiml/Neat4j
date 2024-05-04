package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.ThreadLocalRandom

/**
 * This mutation looks at each connection in a genome, and toggles the enabled
 * state of the connection.
 *
 * The idea behind toggling (instead of just removing a connection) is that you
 * can try a significantly different strategy but still be a similar enough
 * network for speciation.
 *
 * @property neat The [Neat] instance managing this object.
 */
class ToggleMutation(override val neat: Neat) : Mutation {
    override fun mutate(genome: Genome) {
        val rand = ThreadLocalRandom.current()

        if (rand.nextFloat() < neat.parameters.mutationToggleChance) {
            return
        }

        for (connection in genome.connections) {
            if (rand.nextFloat() < neat.parameters.mutationToggleConnectionChance) {
                connection.enabled = !connection.enabled
            }
        }
    }
}