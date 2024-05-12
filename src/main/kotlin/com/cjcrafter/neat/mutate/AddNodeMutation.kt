package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.ThreadLocalRandom

/**
 * This mutation adds a new node to the genome.
 *
 * The idea is to add a node, which allows us to fit "more complicated curves"
 * to our problem, instead of just linear equations. The important part is that
 * adding a node *should not* harm the performance of the network. To accomplish
 * this, we copy the weight of the previous connection, and apply that weight to
 * our 2 new connections. **Note:** Depending on your activation function, this
 * is *not* a perfect way to prevent changes.
 *
 * @property neat The [Neat] instance managing this object.
 */
class AddNodeMutation(override val neat: Neat) : Mutation {
    override fun mutate(genome: Genome) {
        // chance to trigger
        val rand = ThreadLocalRandom.current()
        if (rand.nextFloat() >= neat.parameters.mutationAddNodeChance)
            return

        // If there are no connections, we can't add a node
        if (genome.connections.isEmpty())
            return

        // Select 1 random connection. We're going to replace it with a node
        // and 2 connections.
        val randomConnection = genome.connections[rand.nextInt(genome.connections.size)]
        val middle = neat.getOrCreateReplacementNode(randomConnection)

        // Instead of having a connection from->to, we now have a new node, "middle"
        // So we want from->middle->to instead.
        val a = neat.createConnection(randomConnection.fromId, middle.id)
        val b = neat.createConnection(middle.id, randomConnection.toId)

        // Adjust weights so that this new node shouldn't have any effect on
        // the neural network calculations (until weights get shifted later)
        a.weight = 1f
        b.weight = randomConnection.weight
        b.enabled = randomConnection.enabled

        // Disable the old connection gene, and add the 2 replacement genes
        randomConnection.enabled = false
        genome.connections.add(a)
        genome.connections.add(b)

        // Add the new node into the network
        genome.nodes.add(middle)
    }
}