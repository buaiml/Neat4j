package com.cjcrafter.neat.util

import java.util.*

class ProbabilityMap<E> {

    private class Entry<E>(
        val element: E?,
        val probability: Double,
        val offset: Double,
    ) : Comparable<Entry<E>> {
        override fun compareTo(other: Entry<E>): Int {
            return (offset - other.offset).toInt()
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

    private val treeMap: TreeMap<Entry<E>, Entry<E>> = TreeMap()
    private var total = 0.0

    operator fun set(element: E, probability: Double) {
        val entry = Entry(element, probability, total)
        treeMap[entry] = entry
        total += probability
    }

    fun get(): E {
        val random = Random().nextDouble() * total
        val entry = treeMap.ceilingEntry(Entry(null, random, 0.0))?.value
        return entry?.element!!
    }
}