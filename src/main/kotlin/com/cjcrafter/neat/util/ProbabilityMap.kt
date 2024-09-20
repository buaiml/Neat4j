package com.cjcrafter.neat.util

import java.util.concurrent.ThreadLocalRandom

class ProbabilityMap<E> {

    private class Node<E>(
        val element: E,
        val min: Double,
        val max: Double,
    )

    private val probabilities = mutableListOf<Node<E>>()
    private var total = 0.0

    operator fun set(element: E, probability: Double) {
        if (probabilities.find { it.element == element } != null) {
            throw IllegalArgumentException("$element already exists in the probability map")
        }

        val node = Node(element, total, total + probability)
        probabilities.add(node)
        total += probability
    }

    fun get(): E {
        val random = ThreadLocalRandom.current().nextDouble() * total
        return binarySearch(random).element
    }

    fun isEmpty(): Boolean {
        return probabilities.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return probabilities.isNotEmpty()
    }

    private fun binarySearch(value: Double): Node<E> {
        var low = 0
        var high = probabilities.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val node = probabilities[mid]

            if (node.min <= value && node.max > value) {
                return node
            } else if (node.min > value) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }

        throw IllegalArgumentException("Could not find element for value $value")
    }
}