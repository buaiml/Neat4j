package com.cjcrafter.neat.util

import java.util.Random

/**
 * Returns true with a chance of [chance].
 */
fun Random.chance(chance: Float): Boolean {
    return nextFloat() < chance
}
