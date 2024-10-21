package com.cjcrafter.neat.mutate

import com.cjcrafter.neat.Neat
import com.cjcrafter.neat.NeatInstance
import com.cjcrafter.neat.genome.Genome
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Represents a mutation that can be applied to a genome.
 */
abstract class Mutation : NeatInstance {

    @JsonIgnore
    override lateinit var neat: Neat

    /**
     * Applies this mutation to the given genome.
     */
    abstract fun mutate(genome: Genome)
}