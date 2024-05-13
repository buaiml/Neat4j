package com.cjcrafter.neat

import com.cjcrafter.neat.compute.Calculator
import com.cjcrafter.neat.compute.SimpleCalculator
import com.cjcrafter.neat.genome.Genome

class Client(
    override val neat: Neat,
    val id: Int,
    genome: Genome = neat.createGenome(),
) : NeatInstance, Comparable<Client> {

    var genome = genome
        set(value) {
            field = value
            calculator0 = null
        }
    var score: Double = 0.0
    var species: Species? = null

    private var calculator0: Calculator? = null
    val calculator: Calculator
        get() {
            if (calculator0 == null) {
                calculator0 = SimpleCalculator(genome)
            }
            return calculator0!!
        }

    fun mutate() {
        for (mutation in neat.mutations) {
            mutation.mutate(genome)
        }

        // Since the genome has (probably) changed, we need to reset the
        // calculator to reflect the new genome.
        calculator0 = null
    }

    override fun compareTo(other: Client): Int {
        return score.compareTo(other.score)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Client

        if (neat != other.neat) throw IllegalArgumentException("Cannot compare clients of different Neat instances")
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "Client(id=$id, genome=$genome, score=$score, species=$species)"
    }
}