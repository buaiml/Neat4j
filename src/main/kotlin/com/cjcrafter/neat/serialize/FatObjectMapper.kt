package com.cjcrafter.neat.serialize

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.joml.Vector2f

/**
 * Returns a jackson object mapper with modules built-in to handle objects
 * from the Neat4J library.
 */
fun fatObjectMapper(): ObjectMapper {
    val mapper = jacksonObjectMapper()
    mapper.registerModule(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())

    // Neat4J modules
    val module = SimpleModule()
    module.addSerializer(Vector2f::class.java, Vector2fSerializer())
    module.addDeserializer(Vector2f::class.java, Vector2fDeserializer())
    mapper.registerModule(module)

    return mapper
}
