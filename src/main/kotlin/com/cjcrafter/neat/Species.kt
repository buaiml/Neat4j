package com.cjcrafter.neat

import com.cjcrafter.neat.genome.Genome
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

/**
 * Represents a species of clients. A species is a group of clients that are
 * similar to each other. This is determined by the distance between the genome
 * of the clients.
 *
 * @property neat The [Neat] instance managing this object.
 * @property base The base client of this species.
 * @constructor Create empty Species
 */
class Species(
    override val neat: Neat,
    val id: Int,
    private var base: Client,
): NeatInstance, Comparable<Species> {

    val clients: MutableList<Client> = mutableListOf()
    var score = 0.0
    var generations = 0
    private var isExtinct = false

    init {
        base.species = this
        clients.add(base)
    }

    /**
     * Returns a random client from this species.
     */
    fun random(): Client? {
        if (clients.isEmpty())
            return null

        val index = ThreadLocalRandom.current().nextInt(clients.size)
        return clients[index]
    }

    /**
     * Returns true if the given client is a match for this species.
     *
     * Matching is determined by looking at the distance between the genome of
     * the client and the base genome of this species. If the distance is less
     * than the species distance threshold, then the client is a match.
     *
     * @param client The client to check if it matches this species.
     * @return True if the client is a match for this species.
     */
    fun matches(client: Client): Boolean {
        val distance: Float = base.genome - client.genome
        return distance < neat.parameters.speciesDistanceThreshold
    }

    /**
     * Adds the given client to this species. If the client is a match for this
     * species, then the client is added to this species. If the client is not
     * a match, then the client is not added to this species.
     *
     * @param client The client to add to this species.
     * @param force If true, the client will be added to this species even if it
     * is not a match.
     * @return True if the client was added to this species.
     */
    @JvmOverloads
    fun put(client: Client, force: Boolean = false): Boolean {
        if (isExtinct)
            throw IllegalStateException("Species is extinct")

        if (force || matches(client)) {
            client.species = this
            clients.add(client)
            return true
        }
        return false
    }

    /**
     * Calculates the score of this species. The score is calculated by averaging
     * the score of all the clients in this species.
     */
    fun evaluate() {
        score = 0.0
        for (client in clients) {
            score += client.score
        }
        score /= clients.size

        // when score is exactly 0, we end up with a species that has no chance
        // of breeding. We need to make sure that the final score is non-zero.
        score = score.coerceAtLeast(0.0001)
        generations++
    }

    /**
     * Removes all clients but 1 random client from this species.
     */
    fun reset() {
        score = 0.0

        // Use some random client as the new base
        base = random() ?: base

        // Remove all current clients from the species (many will be resorted
        // back into this species by the managing Neat instance)
        clients.forEach { it.species = null }
        clients.clear()

        // Add the new base client back in
        put(base, true)
    }

    /**
     * Marks this species as extinct. This will remove all clients from this
     * species. This species will no longer be able to breed.
     */
    fun extirpate() {
        isExtinct = true
        clients.forEach { it.species = null }
        clients.clear()
    }

    /**
     * Kills off a percentage of the worst performing clients in this species.
     *
     * @param percentage The percentage of clients to kill off.
     */
    fun kill(percentage: Float) {
        assert(percentage in 0.0..1.0)

        // If the species is young and small, then we should not kill off any...
        // This is to protect species that are creating new innovations.
        if (generations <= neat.parameters.speciesGracePeriod && clients.size <= 2)
            return

        // Sort the clients by their score, so we only kill off the worst
        // performing clients (keeping the strongest clients alive)
        clients.sort()

        // Remove the worst performing clients from this species
        val size = clients.size
        val kill = (size * percentage).roundToInt()

        // since the lowest score is at the beginning of the list, we can just
        // remove the first `kill` clients.
        for (i in 0 until kill) {
            clients[0].species = null
            clients.removeAt(0)
        }

        // If we removed the base client, we need to select a new base client
        if (base !in clients) {
            if (clients.isEmpty()) {
                extirpate()
            } else {
                base = random()!!
            }
        }
    }

    /**
     * Breeds 2 random clients from this species. If there are not enough clients
     * to breed, then this method will return null.
     *
     * @return The offspring genome, or null if there are not enough clients to
     * breed.
     */
    fun breed(): Genome? {
        // Get 2 random clients from this species to breed, or return if there
        // are not enough clients to breed.
        val a = random() ?: return null
        val b = random() ?: return null

        // Make sure the first parent (the main parent) is the one that scores
        // higher. This will cause the child to inherit more from the stronger
        // parent.
        val (main, other) = if (a.score > b.score) a to b else b to a
        return main.genome % other.genome
    }

    override fun compareTo(other: Species): Int {
        return score.compareTo(other.score)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Species

        if (neat != other.neat) throw IllegalArgumentException("Cannot compare species of different Neat instances")
        if (base != other.base) return false

        return true
    }

    override fun hashCode(): Int {
        return base.hashCode()
    }

    override fun toString(): String {
        return "Species(base=${base.id}, score=$score, isExtinct=$isExtinct)"
    }
}