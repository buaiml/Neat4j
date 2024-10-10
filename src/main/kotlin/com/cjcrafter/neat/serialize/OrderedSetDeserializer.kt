package com.cjcrafter.neat.serialize

import com.cjcrafter.neat.util.OrderedSet
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

/**
 * A deserializer for [OrderedSet] objects.
 */
class OrderedSetDeserializer<E : Comparable<E>>(private val elementType: Class<E>) : JsonDeserializer<OrderedSet<E>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OrderedSet<E> {
        val node = p.codec.readTree<JsonNode>(p)
        val elements = node.map { p.codec.treeToValue(it, elementType) }
        val orderedSet = OrderedSet<E>()
        orderedSet.addAll(elements)
        return orderedSet
    }
}