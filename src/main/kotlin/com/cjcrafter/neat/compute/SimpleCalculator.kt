package com.cjcrafter.neat.compute

import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.genome.NodeGene
import java.util.concurrent.CompletableFuture
import kotlin.math.exp

class SimpleCalculator(genome: Genome) : Calculator {

    private val inputs: List<Node>
    private val hidden: List<Node>
    private val outputs: List<Node>

    init {
        val inputs = mutableListOf<Node>()
        val hidden = mutableListOf<Node>()
        val outputs = mutableListOf<Node>()

        // Maps a node gene to the calculator node. This is used later for connections
        val nodeCache = mutableMapOf<Int, Node>()

        // Loop through all nodes in the genome, and sort them into their respective
        // categories. This is important because we need to know which nodes are
        // inputs, hidden, and outputs.
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

        // Sort the hidden nodes by their x position. This is important because
        // connections have to go from left to right, and we need to know the
        // value of a node ON THE LEFT before we can calculate the value of a node
        // ON THE RIGHT.
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

        this.inputs = inputs
        this.hidden = hidden
        this.outputs = outputs
    }

    @Synchronized
    override fun calculate(input: FloatArray): CompletableFuture<FloatArray> {
        if (input.size != inputs.size)
            throw IllegalArgumentException("Input size does not match genome input size")

        // Set input values
        for (i in input.indices)
            inputs[i].output = input[i]

        // Calculate hidden nodes
        for (node in hidden)
            node.calculate()

        // Calculate output nodes
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
            return 1f / (1f + exp(-4.9f * value))
        }

        override fun compareTo(other: Node): Int {
            // connections have to go from left to right
            return x.compareTo(other.x)
        }
    }

    private class Connection(val from: Node, val weight: Float, val enabled: Boolean)
}