package com.cjcrafter.neat.serialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.joml.Vector2f

class Vector2fDeserializer : StdDeserializer<Vector2f>(Vector2f::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Vector2f {
        val node = p.codec.readTree<JsonNode>(p)
        val x = node.get("x").floatValue()
        val y = node.get("y").floatValue()
        return Vector2f(x, y)
    }
}