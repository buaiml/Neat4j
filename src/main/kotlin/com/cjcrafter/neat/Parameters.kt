package com.cjcrafter.neat

/**
 * The configuration options for the NEAT algorithm.
 *
 * @property isFullNetwork When true, networks will start fully connected.
 * @property useBiasNode When true, a bias node will be added to the network.
 * @property disjointCoefficient Determines the strength of disjoint connections.
 * @property excessCoefficient Determines the strength of excess connections.
 * @property weightCoefficient Determines the strength of weight differences.
 * @property speciesDistance The distance threshold for a client to be in the same species.
 * @property mutateWeightChance The chance to trigger a weight mutation.
 * @property mutateWeightShiftChance The chance for 1 weight to shift.
 * @property mutateWeightShiftStrength The strength of the weight shift.
 * @property mutateWeightRandomizeStrength The strength of the weight randomization.
 * @property mutateAddNodeChance The chance to add a new node.
 * @property mutateAddConnectionChance The chance to add a new connection.
 * @property killPercentage The percentage of species to kill.
 * @property speciesGracePeriod The number of generations to wait before killing species.
 * @property stagnationLimit The number of generations to wait before killing off all but the best client.
 * @property targetSpeciesCount The target number of species.
 */
class Parameters @JvmOverloads constructor(
    var isFullNetwork: Boolean = true,
    var useBiasNode: Boolean = true,
    var disjointCoefficient: Float = 1.0f,
    var excessCoefficient: Float = 1.0f,
    var weightCoefficient: Float = 0.4f,
    var speciesDistance: Float = 3.0f,
    var mutateWeightChance: Float = 0.8f,
    var mutateWeightShiftChance: Float = 0.9f,
    var mutateWeightShiftStrength: Float = 0.15f,
    var mutateWeightRandomizeStrength: Float = 1.0f,
    var mutateAddNodeChance: Float = 0.03f,
    var mutateAddConnectionChance: Float = 0.05f,
    var killPercentage: Float = 0.75f,
    var speciesGracePeriod: Int = 1,
    var interspeciesMatingRate: Float = 0.001f,
    var stagnationLimit: Int = 15,
    var minWeight: Float = -30.0f,
    var maxWeight: Float = 30.0f,
    var targetSpeciesCount: Int = 10,
)
