package com.cjcrafter.neat

import com.cjcrafter.neat.genome.ConnectionGene
import com.cjcrafter.neat.genome.Genome
import com.cjcrafter.neat.genome.NodeGene
import com.cjcrafter.neat.mutate.AddConnectionMutation
import com.cjcrafter.neat.mutate.AddNodeMutation
import com.cjcrafter.neat.mutate.Mutation
import com.cjcrafter.neat.mutate.ToggleMutation
import com.cjcrafter.neat.mutate.WeightsMutation
import com.cjcrafter.neat.util.ProbabilityMap
import org.joml.Vector2f
import java.util.concurrent.ThreadLocalRandom

class NeatImpl(
    override val countInputNodes: Int,
    override val countOutputNodes: Int,
    override val countClients: Int,
) : Neat {

    // The "config options" and parameters of this NEAT instance
    override val parameters: Neat.Parameters = object : Neat.Parameters() {}
    override val mutations: List<Mutation> = listOf(
        AddConnectionMutation(this),
        AddNodeMutation(this),
        ToggleMutation(this),
        WeightsMutation(this),
    )

    // Cache these genes to prevent creating duplicates... We share these genes
    // between different genomes by cloning them when needed.
    private val connectionCache: MutableMap<Pair<Int, Int>, ConnectionGene> = mutableMapOf()
    private val nodeCache: MutableList<NodeGene> = mutableListOf()

    // The clients that are managed by this NEAT instance
    override val clients: List<Client>
    override val species: MutableList<Species>
    private var speciesCounter = 0

    init {
        // Create the input nodes, which are on the left side of the neural network
        for (i in 0 until countInputNodes) {
            val position = Vector2f(0.1f, (i.toFloat() + 1) / countInputNodes)
            nodeCache.add(NodeGene(this, i, position))
        }

        // Create the output nodes, which are on the right side of the neural network
        for (i in countInputNodes until countInputNodes + countOutputNodes) {
            val position = Vector2f(0.9f, (i.toFloat() + 1) / countOutputNodes)
            nodeCache.add(NodeGene(this, i, position))
        }

        // Create the default genomes for all the clients
        clients = List(countClients) { id -> Client(this, id) }

        // Create a default species and add all clients to it
        val species = Species(this, speciesCounter++, clients[0])
        clients.subList(1, clients.size).forEach { species.put(it, force = true) }
        this.species = mutableListOf(species)
    }

    override fun createGenome(forceEmpty: Boolean): Genome {
        val genome = Genome(this)

        // The genome starts out empty, so we need to add all the required nodes
        for (i in 0 until countInputNodes + countOutputNodes) {
            val node = getNode(i)
            genome.nodes.add(node)
        }

        if (!forceEmpty && parameters.fullNetwork) {
            // Create a connection between all input and output nodes
            for (input in 0 until countInputNodes) {
                for (output in countInputNodes until countInputNodes + countOutputNodes) {
                    val connection = createConnection(input, output)
                    connection.weight = ThreadLocalRandom.current().nextFloat() * 2 - 1
                    genome.connections.add(connection)
                    genome.connections.sort()
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
            return nodeCache[id]
    }

    override fun createNode(): NodeGene {
        val id = nodeCache.size
        val position = Vector2f()
        val node = NodeGene(this, id, position)
        nodeCache.add(node)
        return node
    }

    override fun getConnectionOrNull(fromId: Int, toId: Int): ConnectionGene? {
        val pair = fromId to toId
        return connectionCache[pair]?.clone() // Return a clone to prevent modification
    }

    override fun createConnection(fromId: Int, toId: Int): ConnectionGene {
        // Check if the connection already exists
        getConnectionOrNull(fromId, toId)?.let { return it }

        // Create a new connection
        val id = connectionCache.size
        val connection = ConnectionGene(this, id, fromId, toId)
        connectionCache[fromId to toId] = connection
        return connection
    }

    override fun getOrCreateReplacementNode(connection: ConnectionGene): NodeGene {
        val connection = getConnectionOrNull(connection.fromId, connection.toId)!!
        if (connection.replacementNode == -1) {
            val node = createNode()
            connection.replacementNode = node.id
            return node
        } else {
            return getNode(connection.replacementNode)
        }
    }

    override fun evolve() {
        // Remove all clients from their species (except for the base client "representative")
        for (species in species) {
            species.reset()
        }

        // Sort each client into a matching
        for (client in clients) {
            if (client.species == null) {
                for (species in species) {
                    if (species.put(client))
                        break
                }
            }

            // If the client still doesn't have a species, then we need to create a new one
            if (client.species == null) {
                val species = Species(this, speciesCounter++, client)
                this.species.add(species)
            }
        }

        // Kill off the worst performing clients in each species
        val iterator = species.iterator()
        while (iterator.hasNext()) {
            val species = iterator.next()
            species.evaluate()
            species.kill(parameters.killPercentage)

            // If all clients in the species are dead, then we should remove
            // the species (and clear the species of the clients that were in it)
            if (species.clients.isEmpty()) {
                species.extirpate()
                iterator.remove()
            }
        }

        // For each client that died, we need to sort it into a species by
        // breeding. It has a higher chance of being sorted into a species
        // with a higher score.
        val probabilityMap = ProbabilityMap<Species>()
        species.forEach { probabilityMap[it] = it.score }
        for (client in clients) {
            if (client.species == null) {
                val species = probabilityMap.get()
                client.genome = species.breed()!!
                client.mutate()
                species.put(client, true)
            }
        }
    }
}