package com.cjcrafter.neat.serialize

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.joml.Vector2f

class Vector2fSerializer : StdSerializer<Vector2f>(Vector2f::class.java) {
    override fun serialize(value: Vector2f, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("x", value.x)
        gen.writeNumberField("y", value.y)
        gen.writeEndObject()
    }
}