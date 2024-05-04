package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.NeatInstance
import com.cjcrafter.neat.genome.Genome

/**
 * Represents a mutation that can be applied to a genome.
 */
interface Mutation : NeatInstance{

    /**
     * Applies this mutation to the given genome.
     */
    fun mutate(genome: Genome)
}