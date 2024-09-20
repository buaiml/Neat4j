package com.cjcrafter.neat.genome

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.NeatInstance
import com.cjcrafter.neat.util.OrderedSet
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

    var nodes = OrderedSet<NodeGene>()
    var connections = OrderedSet<ConnectionGene>()

    internal fun updateNodeCounts(oldIdToNewIdCache: IntArray, nodeCache: List<NodeGene>) {
        val newNodes = OrderedSet<NodeGene>()
        for (node in nodeCache) {
            // copy node over if it is either an input or output node, or if
            // the node is in the old list
            if (node.isInput() || node.isOutput() || node in nodes) {
                val newNode = node.clone()
                newNodes.add(newNode)
            }
        }
        nodes = newNodes

        for (connection in connections) {
            connection.fromId = oldIdToNewIdCache[connection.fromId]
            connection.toId = oldIdToNewIdCache[connection.toId]
        }
    }

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

    override fun toString(): String {
        val hiddenNodes = nodes.size - neat.countInputNodes - neat.countOutputNodes
        val innovations = connections.size
        return "Genome(nodes=${nodes.size}, hidden=$hiddenNodes, innovations=$innovations)"
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

            // We want to iterate over both genomes simultaneously, and copy connections
            var indexA = 0
            var indexB = 0
            while (indexA < a.connections.size && indexB < b.connections.size) {
                val connectionA = a.connections[indexA]
                val connectionB = b.connections[indexB]

                if (connectionA.id == connectionB.id) {
                    addConnection(child, a, connectionA).weight = connectionA.weight * 0.5f + connectionB.weight * 0.5f

                    indexA++
                    indexB++
                } else if (connectionA.id < connectionB.id) {
                    addConnection(child, a, connectionA)
                    indexA++
                } else {
                    // skip adding this connection... probably no good! It is
                    // too different from our "main" parent a
                    addConnection(child, b, connectionB)
                    indexB++
                }
            }

            // Copy over the excess connections from the main parent
            while (indexA < a.connections.size) {
                addConnection(child, a, a.connections[indexA])
                indexA++
            }

            while (indexB < b.connections.size) {
                addConnection(child, b, b.connections[indexB])
                indexB++
            }

            return child
        }

        private fun addConnection(
            child: Genome,
            parent: Genome,
            connection: ConnectionGene,
        ): ConnectionGene {
            val copy = connection.clone()
            child.connections.add(copy)
            tryAddNode(child, parent, copy.fromId)
            tryAddNode(child, parent, copy.toId)
            return copy
        }

        private fun tryAddNode(
            child: Genome,
            parent: Genome,
            nodeId: Int,
        ) {
            val node = parent.nodes.find { it.id == nodeId }?.clone() ?: throw IllegalStateException("Could not find node $nodeId in genome ${parent.nodes}")
            if (node !in child.nodes) {
                child.nodes.add(node)
            }
        }
    }
}