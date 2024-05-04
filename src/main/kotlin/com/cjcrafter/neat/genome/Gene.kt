package com.cjcrafter.neat.genome

import com.cjcrafter.neat.NeatInstance

/**
 * Marks a "gene," which is just a "configurable" part of a [Genome].
 */
sealed interface Gene : NeatInstance, Comparable<Gene> {

    /**
     * The unique id of this gene.
     */
    var id: Int

    /**
     * The type of this gene.
     */
    val type: Type

    override fun compareTo(other: Gene): Int {
        return this.id.compareTo(other.id)
    }

    /**
     * The type of this gene.
     */
    enum class Type {
        /**
         * A [NodeGene] gene.
         */
        NODE,

        /**
         * A [ConnectionGene] gene.
         */
        CONNECTION
    }
}