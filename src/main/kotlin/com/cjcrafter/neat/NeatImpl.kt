package com.cjcrafter.neat

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.genome.NodeGene
import com.cjcrafter.neat.mutate.AddConnectionMutation
import com.cjcrafter.neat.mutate.AddNodeMutation
import com.cjcrafter.neat.mutate.Mutation
import com.cjcrafter.neat.mutate.WeightsMutation
import com.cjcrafter.neat.util.ProbabilityMap
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.joml.Vector2f
import java.util.ArrayList
import java.util.LinkedList
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class NeatImpl(
    override var countInputNodes: Int,
    override var countOutputNodes: Int,
    override var countClients: Int,
    override val parameters: Parameters = Parameters(),
) : Neat {
    override var speciesDistanceFactor = SpeciesDistanceFactor(parameters.speciesDistance).apply { neat = this@NeatImpl }
    override var generationNumber: Int = 0

    // The "config options" and parameters of this NEAT instance
    @JsonIgnore
    override var mutations: List<Mutation> = listOf(
        AddConnectionMutation().apply { neat = this@NeatImpl },
        AddNodeMutation().apply { neat = this@NeatImpl },
        WeightsMutation().apply { neat = this@NeatImpl },
    )

    // Cache these genes to prevent creating duplicates... We share these genes
    // between different genomes by cloning them when needed.
    var connectionCache: MutableMap<ConnectionGene, ConnectionGene> = mutableMapOf()
    var nodeCache: MutableList<NodeGene> = mutableListOf()
    var replacements: MutableMap<ConnectionGene, Int> = mutableMapOf()

    // The clients that are managed by this NEAT instance
    override var clients: List<Client>
    override var allSpecies: MutableList<Species> = mutableListOf()
    private var speciesCounter = 0

    init {
        this.countInputNodes += if (parameters.useBiasNode) 1 else 0

        // Create the input nodes, which are on the left side of the neural network
        for (i in 0 until countInputNodes) {
            val newNode = createNode()
            newNode.position = Vector2f(0.1f, (i.toFloat() + 1) / (countInputNodes + 1))
        }

        // Create the output nodes, which are on the right side of the neural network
        for (i in 0 until countOutputNodes) {
            val newNode = createNode()
            newNode.position = Vector2f(0.9f, (i.toFloat() + 1) / (countOutputNodes + 1))
        }

        // Create the default genomes for all the clients
        clients = List(countClients) { id -> Client(id, createGenome()).apply { neat = this@NeatImpl } }
        sortClientsIntoSpecies()
    }

    fun updateNodeCounts(countInputNodes: Int, countOutputNodes: Int) {
        if (countInputNodes < this.countInputNodes)
            throw IllegalArgumentException("Cannot reduce the number of input nodes")
        if (countOutputNodes < this.countOutputNodes)
            throw IllegalArgumentException("Cannot reduce the number of output nodes")

        val oldNodeCount = nodeCache.size
        val oldCountInputNodes = this.countInputNodes
        val oldCountOutputNodes = this.countOutputNodes
        this.countInputNodes = countInputNodes
        this.countOutputNodes = countOutputNodes

        // Start by inserting new nodes into the cache
        for (i in oldCountInputNodes until countInputNodes) {
            val newNode = NodeGene(i).apply { neat = this@NeatImpl }
            nodeCache.add(i, newNode)
        }
        for (i in oldCountOutputNodes until countOutputNodes) {
            val newNode = NodeGene(i + countInputNodes).apply { neat = this@NeatImpl }
            nodeCache.add(i + countInputNodes, newNode)
        }

        // Update the positions of the nodes
        for (i in 0 until countInputNodes) {
            val node = getNode(i)
            node.position = Vector2f(0.1f, (i.toFloat() + 1) / (countInputNodes + 1))
        }
        for (i in 0 until countOutputNodes) {
            val node = getNode(i + countInputNodes)
            node.position = Vector2f(0.9f, (i.toFloat() + 1) / (countOutputNodes + 1))
        }

        // Update the id of each node
        val oldIdToNewIdCache = IntArray(oldNodeCount)
        for (i in 0 until nodeCache.size) {
            // Happens when we add more output nodes then there are hidden nodes
            if (nodeCache[i].id >= oldNodeCount)
                continue

            oldIdToNewIdCache[nodeCache[i].id] = i
            nodeCache[i].id = i
        }

        // Update the connections
        for (connection in connectionCache.keys) {
            connection.fromId = oldIdToNewIdCache[connection.fromId]
            connection.toId = oldIdToNewIdCache[connection.toId]
        }

        // Look at all the current genomes and update their nodes and
        // connections to match the new ids.
        for (client in clients) {
            client.updateNodeCounts(oldIdToNewIdCache, nodeCache)
        }
    }

    fun updateClients(countClients: Int) {
        val newClients = ArrayList<Client>(countClients)
        for (i in 0 until countClients) {
            if (i < clients.size) {
                newClients.add(clients[i])
            } else {
                newClients.add(Client(i, createGenome()).apply { neat = this@NeatImpl })
            }
        }
        clients = newClients
        sortClientsIntoSpecies()
    }

    override fun createGenome(forceEmpty: Boolean): Genome {
        val genome = Genome().apply { neat = this@NeatImpl }

        // The genome starts out empty, so we need to add all the required nodes
        for (i in 0 until countInputNodes + countOutputNodes) {
            val node = getNode(i)
            genome.nodes.add(node)
        }

        if (!forceEmpty && parameters.isFullNetwork) {
            // Create a connection between all input and output nodes
            for (input in 0 until countInputNodes) {
                for (output in countInputNodes until countInputNodes + countOutputNodes) {
                    val connection = createConnection(input, output)
                    connection.weight = ThreadLocalRandom.current().nextGaussian().toFloat()
                    genome.connections.add(connection)
                }
            }
        }

        return genome
    }

    fun getNode(id: Int): NodeGene {
        if (id < 0 || id > nodeCache.size)
            throw IllegalArgumentException("Invalid node id: $id")
        else if (id == nodeCache.size)
            return createNode()
        else
            return nodeCache[id].clone()
    }

    override fun createNode(): NodeGene {
        val id = nodeCache.size
        val position = Vector2f()
        val node = NodeGene(id, position).apply { neat = this@NeatImpl }
        nodeCache.add(node)
        return node
    }

    override fun getConnectionOrNull(fromId: Int, toId: Int): ConnectionGene? {
        val hash = ConnectionGene(-1, fromId, toId).apply { neat = this@NeatImpl }
        return connectionCache[hash]?.clone() // Return a clone to prevent modification
    }

    override fun createConnection(fromId: Int, toId: Int): ConnectionGene {
        // Check if the connection already exists
        getConnectionOrNull(fromId, toId)?.let { return it }

        // Create a new connection
        val id = connectionCache.size
        val connection = ConnectionGene(id, fromId, toId).apply { neat = this@NeatImpl }
        connectionCache[connection] = connection
        return connection.clone()
    }

    override fun getOrCreateReplacementNode(connection: ConnectionGene): NodeGene {
        val nodeId: Int? = replacements[connection]
        if (nodeId == null) {
            val node = createNode()
            replacements[connection] = node.id

            // Calculate the midpoint of the 2 nodes
            val from: NodeGene = getNode(connection.fromId)
            val to: NodeGene = getNode(connection.toId)
            val midpoint = from.position.lerp(to.position, 0.5f, Vector2f())

            // Jitter the position vertically, so that the connections won't
            // overlap significantly. This is ONLY important for visualization.
            midpoint.y += ThreadLocalRandom.current().nextFloat() * 0.1f - 0.05f

            node.position = midpoint
            return node
        } else {
            return getNode(nodeId)
        }
    }

    /**
     * Sorts all clients into their matching species.
     *
     * Clients will always be in *some* species, but during the evolve()
     * function, a client's genome may no longer match the species. This
     * method will reset all clients' species.
     */
    private fun sortClientsIntoSpecies() {
        // Remove all clients from their species
        for (species in allSpecies) {
            species.reset()
        }

        speciesDistanceFactor.update()
        for (client in clients) {

            // when this is true, this client is the base client for some species
            if (client.speciesId != null)
                continue

            // Try to sort the client into one of the existing species
            var isFoundMatchingSpecies = false
            for (species in allSpecies) {
                isFoundMatchingSpecies = species.put(client)

                if (isFoundMatchingSpecies)
                    break
            }

            // When no species match this client, create a new one!
            if (!isFoundMatchingSpecies) {
                val species = Species(speciesCounter++, client.id).apply { neat = this@NeatImpl }
                client.speciesId = species.id
                allSpecies.add(species)
            }
        }
    }

    override fun evolve() {
        // once we have changed the clients, we have to sort them into their
        // matching species (or create new ones to match!)
        sortClientsIntoSpecies()
        generationNumber++

        // Kill off the worst performing clients in each species
        val iterator = allSpecies.iterator()
        while (iterator.hasNext()) {
            val species = iterator.next()
            species.evaluate()
            species.kill(parameters.killPercentage)

            // If all clients in the species are dead, then we should remove
            // the species (and clear the species of the clients that were in it)
            if (species.clientIds.isEmpty()) {
                species.extirpate()
                iterator.remove()
            }
        }

        // If all species were killed off, then we should create a new species
        if (allSpecies.isEmpty()) {
            val baseClient = clients[ThreadLocalRandom.current().nextInt(clients.size)]
            val species = Species(speciesCounter++, baseClient.id).apply { neat = this@NeatImpl }
            baseClient.speciesId = species.id
            species.evaluate()  // Have a non-zero score
            allSpecies.add(species)
        }

        // For each client that died, we need to sort it into a species by
        // breeding. It has a higher chance of being sorted into a species
        // with a higher score.
        val probabilityMap = ProbabilityMap<Species>()
        allSpecies.forEach { probabilityMap[it] = it.score }
        val eliteClients = LinkedList<Client>()
        for (client in clients) {
            val species = this.allSpecies.find { species: Species -> species.id == client.speciesId }

            // Should never happen
            if (species == null && client.speciesId != null) {
                throw IllegalStateException("Found a member from an extinct species")
            }

            // For interspecies mating, we should take random genomes from
            // random species and breed them together. Since the resulting
            // genome will probably be WEIRD and BAD, create a new species.
            if (species == null && parameters.interspeciesMatingRate > ThreadLocalRandom.current().nextFloat()) {
                val a = probabilityMap.get().random()?.genome
                val b = probabilityMap.get().random()?.genome

                if (a != null && b != null) {
                    client.genome = a % b
                    val species = Species(speciesCounter++, client.id).apply { neat = this@NeatImpl }
                    client.speciesId = species.id
                    species.evaluate()
                    allSpecies.add(species)
                }
            }

            // Client was removed from a species, give it a new one
            if (species == null) {
                val newSpecies = probabilityMap.get()
                client.genome = newSpecies.breed()!!
                client.mutate()
                newSpecies.put(client, true)
            }

            // Never try to change the champion of a species
            else if (client != species.champion) {
                eliteClients.add(client)
            }
        }

        // Elitism: Keep the best performing client in each species
        // But as the species gets more stale, we should mutate them
        for (elite in eliteClients) {
            val species = this.allSpecies.find { species: Species -> species.id == elite.speciesId }

            // Should never happen
            if (species == null) {
                throw IllegalStateException("Found a member from an extinct species")
            }

            val staleness = species.getStaleRate()
            if (staleness > ThreadLocalRandom.current().nextFloat()) {
                elite.mutate()
            }
        }
    }

    override fun serialize(): String {
        val mapper = jacksonObjectMapper()
        mapper.registerModule(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
        return mapper.writeValueAsString(this)
    }

    companion object {
        @JvmStatic
        fun fromJson(json: String): NeatImpl {
            val mapper = jacksonObjectMapper()
            val dto: NeatDTO = mapper.readValue(json)
            val neat = NeatImpl(dto.countInputNodes, dto.countOutputNodes, dto.countClients, dto.parameters)

            // Copy the data from the DTO to the NEAT instance
            neat.speciesDistanceFactor = dto.speciesDistanceFactor
            neat.generationNumber = dto.generationNumber
            neat.connectionCache = dto.connectionCache.toMutableMap()
            neat.nodeCache = dto.nodeCache.toMutableList()
            neat.replacements = dto.replacements.toMutableMap()
            neat.clients = dto.clients
            neat.allSpecies = dto.allSpecies.toMutableList()
            neat.speciesCounter = dto.speciesCounter

            // Update caches to use the proper Neat instance
            for ((key, value) in neat.connectionCache) {
                key.neat = neat
                value.neat = neat
            }
            for (node in neat.nodeCache) {
                node.neat = neat
            }
            for ((key, _) in neat.replacements) {
                key.neat = neat
            }

            // Update clients to use the proper Neat instance
            for (client in neat.clients) {
                client.neat = neat
                client.genome.neat = neat

                for (connection in client.genome.connections) {
                    connection.neat = neat
                }
                for (node in client.genome.nodes) {
                    node.neat = neat
                }
            }

            // Update species to use the proper Neat instance
            for (species in neat.allSpecies) {
                species.neat = neat
            }

            return neat
        }
    }
}