package com.cjcrafter.neat

/**
 * Marks a class that is specific to its [Neat] instance.
 */
interface NeatInstance {

    /**
     * The [Neat] instance used by this object.
     */
    val neat: Neat
}