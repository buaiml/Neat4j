package com.cjcrafter.neat

/**
 * Outlines any class that has a list of clients.
 */
interface ClientHolder {

    /**
     * Returns a reference to the list of clients
     */
    val clients: List<Client>

    /**
     * Returns the best performing client of the group.
     *
     * This may be null if no scoring has been done.
     */
    val champion: Client?
        get() = clients.maxByOrNull { it.score }
}