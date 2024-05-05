package com.cjcrafter.neat.util

import java.util.*

class ProbabilityMap<E> {

    private class Entry<E>(
        val element: E?,
        val probability: Double,
        val offset: Double,
    ) : Comparable<Entry<E>> {
        override fun compareTo(other: Entry<E>): Int {
            return offset.compareTo(other.offset)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Entry<*>

            if (element != other.element) return false
            return true
        }

        override fun hashCode(): Int {
            return element?.hashCode() ?: 0
        }
    }

    private val splittableRandom = SplittableRandom()
    private val treeMap: TreeMap<Entry<E>, Entry<E>> = TreeMap()
    private var total = 0.0

    operator fun set(element: E, probability: Double) {
        val entry = Entry(element, probability, total)
        treeMap[entry] = entry
        total += probability
    }

    fun get(): E {
        val random = splittableRandom.nextDouble() * total
        val entry = treeMap.floorKey(Entry(null, 0.0, random))
        return entry?.element!!
    }
}