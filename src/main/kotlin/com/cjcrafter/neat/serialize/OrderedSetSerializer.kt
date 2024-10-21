package com.cjcrafter.neat.serialize

import com.cjcrafter.neat.util.OrderedSet
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * A serializer for [OrderedSet] objects.
 */
class OrderedSetSerializer<E : Comparable<E>> : JsonSerializer<OrderedSet<E>>() {
    override fun serialize(value: OrderedSet<E>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        for (element in value) {
            gen.writeObject(element)
        }
        gen.writeEndArray()
    }
}