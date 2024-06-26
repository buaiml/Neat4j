package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.ThreadLocalRandom

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
 *
 * @property neat The [Neat] instance managing this object.
 */
class WeightsMutation(override val neat: Neat) : Mutation {
    override fun mutate(genome: Genome) {
        val rand = ThreadLocalRandom.current()

        // Chance to trigger the weight mutation in the first place
        if (rand.nextFloat() < neat.parameters.mutateWeightChance) {
            return
        }

        // Each connection has a chance to be mutated
        for (connection in genome.connections) {
            mutateOne(rand, connection)
        }
    }

    private fun mutateOne(random: ThreadLocalRandom, connection: ConnectionGene) {
        if (random.nextFloat() < neat.parameters.mutateWeightShiftChance) {
            connection.weight += random.nextGaussian().toFloat() * neat.parameters.mutateWeightShiftStrength
        } else {
            connection.weight = random.nextGaussian().toFloat() * neat.parameters.mutateWeightRandomizeStrength
        }
    }
}
