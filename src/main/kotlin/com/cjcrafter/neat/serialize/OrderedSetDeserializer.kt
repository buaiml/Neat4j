package com.cjcrafter.neat.serialize

import com.cjcrafter.neat.util.OrderedSet
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * A deserializer for [OrderedSet] objects.
 */
class OrderedSetDeserializer<E : Comparable<E>>(private val elementType: JavaType? = null) : StdDeserializer<OrderedSet<E>>(OrderedSet::class.java), ContextualDeserializer {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OrderedSet<E> {
        val node = p.codec.readTree<JsonNode>(p)
        val elements = node.map {
            p.codec.treeToValue(it, elementType!!.rawClass)
        }.map { it as E }

        val orderedSet = OrderedSet<E>()
        orderedSet.addAll(elements)
        return orderedSet
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val elementType = ctxt.contextualType.containedType(0)
        return OrderedSetDeserializer<Comparable<Comparable<*>>>(elementType) as JsonDeserializer<OrderedSet<E>>
    }
}
