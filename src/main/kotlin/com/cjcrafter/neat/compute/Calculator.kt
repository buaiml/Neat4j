package com.cjcrafter.neat.compute

import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.CompletableFuture

/**
 * Wraps a neural network (a [Genome]) and provides a method to calculate the
 * output of the network given an input.
 */
interface Calculator {
    /**
     * Returns the activation of the node with the given ID.
     *
     * This activation is the output of node from the last calculation. It
     * should not be used for any important capacity... It may be used for
     * debugging or visualization.
     *
     * @param nodeId The ID of the node to get the activation of.
     * @return The activation of the node.
     */
    fun getActivation(nodeId: Int): Float

    /**
     * Calculates the output of the neural network given the input.
     *
     * @param input The input to the neural network.
     * @return The output of the neural network.
     */
    fun calculate(input: FloatArray): CompletableFuture<FloatArray>
}
