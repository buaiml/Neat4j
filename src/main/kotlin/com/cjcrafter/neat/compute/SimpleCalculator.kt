package com.cjcrafter.neat.compute

import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.serialize.fatObjectMapper
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.concurrent.CompletableFuture
import kotlin.math.exp

/**
 * Wraps the nodes/connections of a [Genome] so you can calculate outputs.
 *
 * The calculator only works if connections flow left->right.
 */
class SimpleCalculator : Calculator {

    @JsonProperty("isAddBias") private val isAddBias: Boolean
    @JsonProperty("inputs") private val inputs = mutableListOf<Node>()
    @JsonProperty("hidden") private val hidden = mutableListOf<Node>()
    @JsonProperty("outputs") private val outputs = mutableListOf<Node>()
    @JsonIgnore private val nodeCache = mutableMapOf<Int, Node>()

    @JsonCreator
    constructor(
        @JsonProperty("isAddBias") isAddBias: Boolean,
        @JsonProperty("inputs") inputs: List<Node>,
        @JsonProperty("hidden") hidden: List<Node>,
        @JsonProperty("outputs") outputs: List<Node>,
    ) {
        this.isAddBias = isAddBias
        this.inputs.addAll(inputs)
        this.hidden.addAll(hidden)
        this.outputs.addAll(outputs)

        for (node in inputs + hidden + outputs)
            nodeCache[node.id] = node
    }

    constructor(genome: Genome) {
        isAddBias = genome.neat.parameters.useBiasNode

        // Sorts the nodes into their respective lists
        for (node in genome.nodes) {
            val newNode = Node(node.id, node.position.x())
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
                connection.fromId,
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
        if (isAddBias) {
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
            node.calculate(nodeCache)
        for (node in outputs)
            node.calculate(nodeCache)

        // Map the output values into an array to return
        val output = outputs.map { it.output }.toFloatArray()
        return CompletableFuture.completedFuture(output)
    }


    /**
     * Simple representation of a node in the calculator.
     */
    class Node(val id: Int, val x: Float) : Comparable<Node> {
        @JsonIgnore var output: Float = 0f
        val incoming: MutableList<Connection> = mutableListOf()

        fun calculate(nodeCache: Map<Int, Node>) {
            var sum = 0.0
            for (connection in incoming) {
                if (connection.enabled)
                    sum += nodeCache[connection.fromId]!!.output * connection.weight
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

    /**
     * Simple representation of a connection between two nodes.
     */
    class Connection(val fromId: Int, val weight: Float, val enabled: Boolean)

    companion object {
        @JvmStatic
        fun fromJson(json: String): SimpleCalculator {
            val mapper = fatObjectMapper()
            return mapper.readValue(json, SimpleCalculator::class.java)
        }
    }
}