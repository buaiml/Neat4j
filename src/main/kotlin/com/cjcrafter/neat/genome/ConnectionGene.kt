package com.cjcrafter.neat.genome

import com.cjcrafter.neat.Neat

/**
 * Represents a connection between two [NodeGene]s in a neural network.
 *
 * A connection always "flows" left->right (from the node represented by [fromId]
 * to the node represented by [toId]).
 *
 * You should never instantiate this class directly. Instead, use the
 * [Neat.createConnection] method.
 *
 * @property neat The [Neat] instance managing this object.
 * @property id The unique identifier of this connection.
 * @property fromId The id of the node this connection is coming from.
 * @property toId The id of the node this connection is going to.
 */
class ConnectionGene(
    override val neat: Neat,
    override var id: Int,
    val fromId: Int,
    val toId: Int,
): Gene, Cloneable {
    override val type = Gene.Type.CONNECTION

    /**
     * The weight of this connection. When taking in an input from the [fromId]
     * node, this weight is multiplied by the input value. Thus, 0.0 means no
     * association, while a higher value means a stronger association.
     */
    var weight = 0.0f
        set(value) {
            if (value.isNaN() || value.isInfinite())
                throw IllegalArgumentException("Invalid weight: $value")
            field = value.coerceIn(neat.parameters.minWeight, neat.parameters.maxWeight)
        }

    /**
     * Whether this connection is enabled. When disabled, the connection is
     * effectively removed from the network, as if it doesn't exist. However,
     * a mutation can re-enable it.
     */
    var enabled = true

    public override fun clone(): ConnectionGene {
        // This method is used to "share" connections between multiple Genome
        // instances, while still allowing them to be modified independently.
        // 1 connection may be shared between multiple genomes when their
        // fromId and toId both match

        try {
            return super.clone() as ConnectionGene
        } catch (ex: CloneNotSupportedException) {
            throw AssertionError()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionGene

        if (neat != other.neat) throw IllegalArgumentException("Cannot compare genes of different Neat instances")
        if (fromId != other.fromId) return false
        if (toId != other.toId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromId
        result = 31 * result + toId
        return result
    }

    override fun toString(): String {
        return "ConnectionGene(id=$id, fromId=$fromId, toId=$toId, weight=$weight, enabled=$enabled)"
    }
}