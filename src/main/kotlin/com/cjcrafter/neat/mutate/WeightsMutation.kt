package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.util.chance

/**
 * This mutation looks at each connection in a genome, and shifts the weights
 * around randomly. Most of the changes are minor.
 *
 * The idea behind *shifting* the weights is that most weights are probably
 * close to their "perfect" value (since, over time, the network has learned).
 * So the minor shifts are used to further optimize closer to the perfect value.
 *
 * The idea behind *randomizing* the weights is that we might be in a local
 * maximum, and need to explore other areas of the solution space. By randomizing
 * the weight of a connection, we might significantly impact the phenotype of
 * the neural network.
 */
class WeightsMutation : Mutation() {

    override fun mutate(genome: Genome) {

        // Chance to trigger the weight mutation in the first place
        if (!neat.random.chance(neat.parameters.mutateWeightChance))
            return

        // Each connection has a chance to be mutated
        for (connection in genome.connections) {
            mutateOne(connection)
        }
    }

    private fun mutateOne(connection: ConnectionGene) {
        val random = connection.neat.random
        if (random.chance(neat.parameters.mutateWeightShiftChance)) {
            connection.weight += random.nextGaussian().toFloat() * neat.parameters.mutateWeightShiftStrength
        } else {
            connection.weight = random.nextGaussian().toFloat() * neat.parameters.mutateWeightRandomizeStrength
        }
    }
}
