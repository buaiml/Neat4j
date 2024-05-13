package com.cjcrafter.neat

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.genome.NodeGene
import com.cjcrafter.neat.mutate.Mutation

interface Neat : ClientHolder {

    /**
     * The number of input nodes in the neural network.
     *
     * The unique identifier of an input node is always in the range of
     * [0, countInputNodes).
     */
    val countInputNodes: Int

    /**
     * The number of output nodes in the neural network.
     *
     * The unique identifier of an output node is always in the range of
     * [countInputNodes, countInputNodes + countOutputNodes).
     */
    val countOutputNodes: Int

    /**
     * The number of clients that to be managed by this NEAT instance.
     */
    val countClients: Int

    /**
     * Returns the parameters of this NEAT instance.
     */
    val parameters: Parameters

    /**
     * Returns the list of mutations that can be applied to a genome.
     */
    val mutations: List<Mutation>

    val allSpecies: List<Species>

    fun createGenome(): Genome = createGenome(false)

    fun createGenome(forceEmpty: Boolean): Genome

    fun createNode(): NodeGene

    fun getConnectionOrNull(fromId: Int, toId: Int): ConnectionGene?

    fun createConnection(fromId: Int, toId: Int): ConnectionGene

    /**
     * Returns the node that replaces the given connection during a
     * [com.cjcrafter.neat.mutate.AddNodeMutation].
     *
     * This method is an optimization to reduce the number of nodes creates. The
     * rule is: If we are creating a node between 2 nodes, we check if we have
     * ever created a node between those 2 nodes before. If we did, then we should
     * re-use that node id instead of consuming a new one.
     *
     * @param connection The connection to replace with a new node.
     * @return The cached node, or the newly created node.
     */
    fun getOrCreateReplacementNode(connection: ConnectionGene): NodeGene

    fun evolve()
}