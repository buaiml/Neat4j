package com.cjcrafter.neat.compute

import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.CompletableFuture
import kotlin.math.exp

/**
 * Wraps the nodes/connections of a [Genome] so you can calculate outputs.
 *
 * The calculator only works if connections flow left->right.
 *
 * @param genome The genome to base the calculator off of
 */
class SimpleCalculator(private val genome: Genome) : Calculator {

    private val inputs = mutableListOf<Node>()
    private val hidden = mutableListOf<Node>()
    private val outputs = mutableListOf<Node>()
    private val nodeCache = mutableMapOf<Int, Node>()

    init {
        // Sorts the nodes into their respective lists
        for (node in genome.nodes) {
            val newNode = Node(node.position.x())
            nodeCache[node.id] = newNode

            when {
                node.isInput() -> inputs.add(newNode)
                node.isHidden() -> hidden.add(newNode)
                node.isOutput() -> outputs.add(newNode)
                else -> throw IllegalStateException("Node $node is not input, hidden, or output")
            }
        }

        // We need connections to be left->right
        hidden.sort()

        // Loop through all connections, and add connections to our "calculator nodes"
        for (connection in genome.connections) {
            val newConnection = Connection(
                nodeCache[connection.fromId]!!,
                connection.weight,
                connection.enabled
            )

            // Add this new connection to the node it is "flowing into"
            val output = nodeCache[connection.toId]!!
            output.incoming.add(newConnection)
        }
    }

    override fun getActivation(nodeId: Int): Float {
        return nodeCache[nodeId]?.output ?: throw IllegalArgumentException("Node $nodeId does not exist")
    }

    @Synchronized
    override fun calculate(input: FloatArray): CompletableFuture<FloatArray> {
        var input = input
        if (genome.neat.parameters.useBiasNode) {
            val newInput = FloatArray(input.size + 1)
            newInput[0] = 1f
            System.arraycopy(input, 0, newInput, 1, input.size)
            input = newInput
        }

        if (input.size != inputs.size)
            throw IllegalArgumentException("Expected ${inputs.size} inputs, but got ${input.size}")

        // Order is important... Fill inputs first, calculate left->right
        for (i in input.indices)
            inputs[i].output = input[i]
        for (node in hidden)
            node.calculate()
        for (node in outputs)
            node.calculate()

        // Map the output values into an array to return
        val output = outputs.map { it.output }.toFloatArray()
        return CompletableFuture.completedFuture(output)
    }


    private class Node(val x: Float) : Comparable<Node> {
        var output: Float = 0f
        val incoming: MutableList<Connection> = mutableListOf()

        fun calculate() {
            var sum = 0.0
            for (connection in incoming) {
                if (connection.enabled)
                    sum += connection.from.output * connection.weight
            }
            output = activate(sum.toFloat())
        }

        fun activate(value: Float): Float {
            // This factor is used to make the sigmoid function *almost* linear around 0
            val factor = 4.9f
            return 1f / (1f + exp(-factor * value))
        }

        override fun compareTo(other: Node): Int {
            // connections have to go from left to right
            return x.compareTo(other.x)
        }
    }

    private class Connection(val from: Node, val weight: Float, val enabled: Boolean)
}