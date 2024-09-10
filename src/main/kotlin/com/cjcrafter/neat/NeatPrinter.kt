package com.cjcrafter.neat

import de.m3y.kformat.Table
import de.m3y.kformat.table
import java.io.PrintStream

/**
 * Prints the state of a NEAT instance.
 *
 * This can be used to debug the progress of the NEAT instance. Often, you will
 * want to examine the state of the [Neat] object between calls to [Neat.evolve].
 *
 * @property neat The NEAT instance to print.
 */
class NeatPrinter(
    private val neat: Neat,
) {

    /**
     * Returns the average number of hidden nodes and connections in all clients.
     *
     * This method takes [Neat.Parameters.fullNetwork] into account, removing the
     * number of connections that are added by default. This way, you can see the
     * number of innovations made due to the evolution process.
     */
    @JvmOverloads
    fun averages(clients: List<Client> = neat.clients): Triple<Float, Float, Float> {
        var nodes = 0
        var connections = 0
        var score = 0f

        for (client in clients) {
            nodes += client.genome.nodes.size - neat.countInputNodes - neat.countOutputNodes
            connections += client.genome.connections.size
            score += client.score.toFloat()

            if (neat.parameters.isFullNetwork) {
                connections -= neat.countInputNodes * neat.countOutputNodes
            }
        }

        return Triple(nodes.toFloat() / clients.size, connections.toFloat() / clients.size, score / clients.size)
    }

    fun render(): StringBuilder {
        return table {
            header("Species", "Clients", "Score", "Nodes", "Connections", "Staleness")
            for (species in neat.allSpecies) {
                val (nodes, connections, score) = averages(species.clients)
                row("#${species.id}", species.clients.size, score, nodes, connections, species.staleness)
            }

            val (nodes, connections, score) = averages()
            row("Total", neat.clients.size, score, nodes, connections)

            // Extra info
            line("Generation #${neat.generationNumber}")
            line("Total Nodes: ${(neat as NeatImpl).nodeCache.size}")
            line("Total Connections: ${neat.connectionCache.size}")
            line("Species Distance: ${neat.speciesDistanceFactor.speciesDistance}")

            hints {
                precision("Score", 2)
                precision("Nodes", 1)
                precision("Connections", 1)
                borderStyle = Table.BorderStyle.SINGLE_LINE
            }
        }.render()
    }
}