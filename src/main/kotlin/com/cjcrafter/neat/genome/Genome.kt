package com.cjcrafter.neat.genome

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.NeatInstance
import java.util.BitSet
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.max

/**
 * Represents a neural network.
 *
 * Has a list of nodes, and connections between those nodes.
 *
 * @property neat The [Neat] instance managing this object.
 */
class Genome(
    override val neat: Neat,
) : NeatInstance {

    val nodes = ArrayList<NodeGene>()
    val connections = ArrayList<ConnectionGene>()

    /**
     * Calculates the distance between this genome and another genome.
     *
     * Measuring the distance between two genomes is a way to determine how
     * similar they are. This is useful for speciation, which groups similar
     *
     * @param other The other genome to compare to.
     * @return The distance between the two genomes.
     */
    fun distance(other: Genome): Float {
        return distance(neat, this, other)
    }

    /**
     * Same as [distance], but allows you to use the `-` operator.
     */
    operator fun minus(other: Genome): Float {
        return distance(neat, this, other)
    }

    /**
     * Creates an offspring genome using the networks of this, and [other] as
     * parents.
     */
    fun crossOver(other: Genome): Genome {
        return crossOver(neat, this, other)
    }

    /**
     * Same as [crossOver], but allows you to use the `%` operator.
     */
    operator fun rem(other: Genome): Genome {
        return crossOver(neat, this, other)
    }

    companion object {
        internal fun distance(
            neat: Neat,
            a: Genome,
            b: Genome,
        ): Float {

            // Make sure genome a has the "final" connection. This hack works
            // because the connections are sorted by id, and the final connection
            // is always the highest id.
            val isSmaller = (a.connections.lastOrNull()?.id ?: 0) < (b.connections.lastOrNull()?.id ?: 0)
            val (a, b) = if (isSmaller) b to a else a to b

            // Track the number of connections that exist in exactly 1 of the
            // 2 genomes. These are disjoint connections.
            var disjoint = 0
            // Track the number of connections that exist in both genomes
            var similar = 0
            // Accumulate the weight difference of connections that exist in
            // both genomes. Used to find the average weight difference later.
            var weightDiff = 0f

            // We want to iterate over both genomes simultaneously, and compare
            // the connections. When we find a disjoint connection, we want to
            // "pause" the other iterator, so the disjoint iterator can "catch up"
            var indexA = 0
            var indexB = 0
            while (indexA < a.connections.size && indexB < b.connections.size) {
                val connectionA = a.connections[indexA]
                val connectionB = b.connections[indexB]

                if (connectionA.id == connectionB.id) {
                    // The connections are the same, so we can compare them
                    similar++
                    weightDiff += abs(connectionA.weight - connectionB.weight)
                    indexA++
                    indexB++
                } else if (connectionA.id < connectionB.id) {
                    // Connection A is not in B, so it is disjoint
                    disjoint++
                    indexA++
                } else {
                    // Connection B is not in A, so it is disjoint
                    disjoint++
                    indexB++
                }
            }

            // If there are any remaining connections in either genome, they are
            // "excess" connections. These are typically penalized less than
            // disjoint connections, since excess connections tend to occur
            // when a very similar genome is mutated.
            val excess = a.connections.size - indexA

            // Find the average weight diff
            if (similar != 0)
                weightDiff /= similar

            var n = max(a.connections.size, b.connections.size)
            if (n < 20) {
                // Small genomes are penalized more for disjoint and excess
                n = 1
            }

            // Calculate distance using a linear combination
            return neat.parameters.disjointCoefficient * disjoint / n +
                    neat.parameters.excessCoefficient * excess / n +
                    neat.parameters.weightCoefficient * weightDiff
        }

        fun crossOver(
            neat: Neat,
            a: Genome,
            b: Genome,
        ): Genome {

            // Create a new empty genome, so we can accumulate the properties
            // its parents
            val child = neat.createGenome(true)

            // BitTable used to prevent duplicating 1 node. This table basically
            // just acts like a Set<NodeGene>, and we can quickly check if a gene
            // with a specific id (it's index in the bit table) is already added
            val bitTable = BitSet(max(a.nodes.last().id, b.nodes.last().id))

            // The bittable is empty, so we need to add the input and output nodes
            for (i in 0 until neat.countInputNodes + neat.countOutputNodes) {
                bitTable.set(child.nodes[i].id)
            }

            // We want to iterate over both genomes simultaneously, and copy connections
            var indexA = 0
            var indexB = 0
            while (indexA < a.connections.size && indexB < b.connections.size) {
                val connectionA = a.connections[indexA]
                val connectionB = b.connections[indexB]

                if (connectionA.id == connectionB.id) {
                    val flag = ThreadLocalRandom.current().nextBoolean() // 50% chance
                    if (flag) {
                        addConnection(bitTable, child, a, connectionA)
                    } else {
                        addConnection(bitTable, child, b, connectionB)
                    }

                    indexA++
                    indexB++
                } else if (connectionA.id < connectionB.id) {
                    addConnection(bitTable, child, a, connectionA)
                    indexA++
                } else {
                    // skip adding this connection... probably no good! It is
                    // too different from our "main" parent a
                    indexB++
                }
            }

            // Copy over the excess connections from the main parent
            while (indexA < a.connections.size) {
                addConnection(bitTable, child, a, a.connections[indexA])
                indexA++
            }

            // We have been adding nodes randomly... We *NEED* that data to be
            // sorted for this method to work, assuming this child will become
            // a parent during a future iteration.
            child.nodes.sort()

            return child
        }

        private fun addConnection(
            nodeTable: BitSet,
            child: Genome,
            parent: Genome,
            connection: ConnectionGene,
        ) {
            // First, actually add the connection object
            child.connections.add(connection.clone())

            // Try to add the "from" node gene to the genome
            if (!nodeTable[connection.fromId]) {
                //val index = parent.nodes.binarySearch { it.id.compareTo(connection.fromId) }
                val index = parent.nodes.indexOfFirst { it.id == connection.fromId }
                if (index == -1)
                    throw IllegalStateException("Could not find node ${connection.fromId} in genome ${parent.nodes}")
                val node = parent.nodes[index]
                child.nodes.add(node)

                // update the bit table
                nodeTable.set(connection.fromId)
            }

            // Try to add the "from" node gene to the genome
            if (!nodeTable[connection.toId]) {
                //val index = parent.nodes.binarySearch { it.id.compareTo(connection.toId) }
                val index = parent.nodes.indexOfFirst { it.id == connection.toId }
                if (index == -1)
                    throw IllegalStateException("Could not find node ${connection.toId} in genome ${parent.nodes}")
                val node = parent.nodes[index]
                child.nodes.add(node)

                // update the bit table
                nodeTable.set(connection.fromId)
            }
        }
    }

    override fun toString(): String {
        val hiddenNodes = nodes.size - neat.countInputNodes - neat.countOutputNodes
        val innovations = connections.size
        return "Genome(nodes=${nodes.size}, hidden=$hiddenNodes, innovations=$innovations)"
    }
}