package com.cjcrafter.neat.compute

import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.CompletableFuture

/**
 * Wraps a neural network (a [Genome]) and provides a method to calculate the
 * output of the network given an input.
 */
interface Calculator {
    /**
     * Calculates the output of the neural network given the input.
     *
     * @param input The input to the neural network.
     * @return The output of the neural network.
     */
    fun calculate(input: FloatArray): CompletableFuture<FloatArray>
}
