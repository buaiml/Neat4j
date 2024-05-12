package com.cjcrafter.neat.util

class OrderedSet<E : Comparable<E>> : MutableIterable<E> {
    private val list = mutableListOf<E>()
    private val map = mutableMapOf<E, E>()

    val size: Int
        get() = list.size

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return list.isNotEmpty()
    }

    fun add(element: E): Boolean {
        if (element !in map) {
            map[element] = element
            list.add(element)
            list.sort()
            return true
        }
        return false
    }

    fun addAll(elements: Collection<E>): Boolean {
        var addedOne = false
        for (element in elements) {
            if (element !in map) {
                map[element] = element
                list.add(element)
                addedOne = true
            }
        }
        if (addedOne) {
            list.sort()
            return true
        }
        return false
    }

    operator fun contains(element: E): Boolean {
        return element in map
    }

    operator fun get(index: Int): E {
        return list[index]
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private val iterator = list.iterator()
            lateinit var last: E

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): E {
                last = iterator.next()
                return last
            }

            override fun remove() {
                iterator.remove()
                map.remove(last)
            }

        }
    }
}