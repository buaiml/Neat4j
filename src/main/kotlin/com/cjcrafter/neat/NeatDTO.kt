package com.cjcrafter.neat

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.NodeGene

/**
 * A data transfer object for the NEAT algorithm. This is used to prevent cyclical
 * dependencies between the different classes in the NEAT algorithm that implement
 * [NeatInstance].
 */
data class NeatDTO(
    var countInputNodes: Int,
    var countOutputNodes: Int,
    var countClients: Int,
    var parameters: Parameters,
    var speciesDistanceFactor: SpeciesDistanceFactor,
    var generationNumber: Int,
    var connectionCache: LinkedHashSet<ConnectionGene>,
    var nodeCache: List<NodeGene>,
    var replacements: LinkedHashMap<Int, Int>,
    var clients: List<Client>,
    var allSpecies: MutableList<Species>,
    var speciesCounter: Int,
)
