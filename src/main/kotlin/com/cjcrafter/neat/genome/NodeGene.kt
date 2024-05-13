package com.cjcrafter.neat.genome

import com.cjcrafter.neat.Neat
import org.joml.Vector2f
import org.joml.Vector2fc

/**
 * Represents a node in the neural network.
 *
 * You should never instantiate this class directly. Instead, use the
 * [Neat.createNode] method.
 *
 * @property neat The [Neat] instance managing this object.
 * @property id The unique identifier of this node.
 * @property position The position of this node in the network.
 */
class NodeGene internal constructor(
    override val neat: Neat,
    override var id: Int,
    var position: Vector2fc = Vector2f(),
) : Gene, Cloneable {
    override val type: Gene.Type = Gene.Type.NODE

    /**
     * Returns true if this node is an input node.
     *
     * An input node is a node that receives input from the "environment".
     * Before any value can be calculated, we must have the input nodes set.
     */
    fun isInput(): Boolean {
        return id >= 0 && id < neat.countInputNodes
    }

    /**
     * Returns true if this node is an output node.
     *
     * An output node is a node that sends output to the "environment".
     * After all calculations are done, we can read the output nodes to get the
     * result of the neural network.
     */
    fun isOutput(): Boolean {
        return id >= neat.countInputNodes && id < neat.countInputNodes + neat.countOutputNodes
    }

    /**
     * Returns true if this node is a hidden node.
     *
     * A hidden node is a node that is neither an input nor an output node, and
     * is just used for internal calculations in the neural network.
     */
    fun isHidden(): Boolean {
        return id >= neat.countInputNodes + neat.countOutputNodes
    }

    public override fun clone(): NodeGene {
        // This method is used to "share" connections between multiple Genome
        // instances, while still allowing them to be modified independently.
        // 1 connection may be shared between multiple genomes when their
        // fromId and toId both match

        try {
            return super.clone() as NodeGene
        } catch (ex: CloneNotSupportedException) {
            throw AssertionError()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeGene

        // Comparing genes of different Neat instances is not allowed
        if (neat !== other.neat) throw IllegalArgumentException("Cannot compare genes of different Neat instances")
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        // each node has a unique id. No need for bit manipulation, we have a
        // "perfect" hash function :)
        return id
    }

    override fun toString(): String {
        val type = when {
            isInput() -> "Input"
            isOutput() -> "Output"
            else -> "Hidden"
        }
        return "NodeGene(id=$id, position=$position, type=$type)"
    }
}
