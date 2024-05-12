package com.cjcrafter.neat

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.genome.NodeGene
import com.cjcrafter.neat.mutate.Mutation

interface Neat {

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

    val clients: List<Client>

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

    // parameters
    abstract class Parameters {

        /**
         * If true, each network will start fully connected. This means that every
         * input node will be connected to every output node. If false, then each
         * network will start with no connections.
         */
        var fullNetwork: Boolean = true

        /**
         * Used by [com.cjcrafter.neat.genome.Genome.distance] to determine the
         * weight of a disjoint connection (connection present in exactly 1
         * genome).
         */
        var disjointCoefficient: Float = 1f

        /**
         * Used by [com.cjcrafter.neat.genome.Genome.distance] to determine the
         * weight of an excess connection (mutated connection present in
         * exactly 1 genome).
         */
        var excessCoefficient: Float = 1f

        /**
         * Used by [com.cjcrafter.neat.genome.Genome.distance] to determine the
         * weight of the difference of weights in similar connections. Typically,
         * you may use a smaller number (<1) here. If your problem requires fine
         * tweaking of weights, then you might want to make this factor bigger
         * (~3.0)
         */
        var weightCoefficient: Float = 0.4f

        /**
         * Used to determine if 2 genomes are similar enough to be in the same
         * species. Typically, you only change this number when you increase one
         * of [disjointCoefficient], [excessCoefficient], or [weightCoefficient]
         * (to make room for the increased difference).
         */
        var speciesDistanceThreshold: Float = 3f

        /**
         * Used by [com.cjcrafter.neat.genome.Genome.crossOver] to determine the
         * chance that a connection gene will be disabled, as long as at least 1
         * parent had the connection disabled.
         */
        var disabledConnectionChance: Float = 0.75f

        /**
         * Used by [com.cjcrafter.neat.mutate.WeightsMutation] for the chance
         * to trigger itself.
         */
        var mutationWeightChance: Float = 0.8f

        /**
         * Used by [com.cjcrafter.neat.mutate.WeightsMutation] for the chance
         * to trigger a weight shift in a connection. This is a minor perturbation,
         * which is intended to optimize an already accurate weight to a better
         * value.
         */
        var mutationWeightShiftChance: Float = 0.9f

        /**
         * Used by [com.cjcrafter.neat.mutate.WeightsMutation] for the amount
         * to shift the weight of a connection. Typically, you want this to be
         * a minor perturbation, so <0.2 is a good value.
         */
        var mutationWeightShiftAmount: Float = 0.15f

        /**
         * Used by [com.cjcrafter.neat.mutate.ToggleMutation] for the chance
         * to trigger itself.
         */
        var mutationToggleChance: Float = 0.1f

        /**
         * Used by [com.cjcrafter.neat.mutate.ToggleMutation] for the chance
         * to toggle a connection.
         */
        var mutationToggleConnectionChance: Float = 0.1f

        /**
         * Used by [com.cjcrafter.neat.mutate.AddNodeMutation] for the chance
         * to trigger itself. This mutation adds a node to the genome.
         */
        var mutationAddNodeChance: Float = 0.03f

        /**
         * Used by [com.cjcrafter.neat.mutate.AddConnectionMutation] for the
         * chance to trigger itself. This mutation adds a connection to the
         * genome.
         */
        var mutationAddConnectionChance: Float = 0.05f

        /**
         * The percentage of each species to kill off.
         */
        var killPercentage: Float = 0.75f

        /**
         * The number of generations to wait before starting to kill of species.
         */
        var speciesGracePeriod: Int = 1

        /**
         * When the score of the best client in a species has not improved for
         * this number of generations, the species is considered stagnant. When
         * a species is stagnant, only the highest scoring client is allowed to
         * reproduce, all other clients are killed off.
         */
        var stagnationLimit: Int = 15
    }
}