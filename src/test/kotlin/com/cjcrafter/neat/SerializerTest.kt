package com.cjcrafter.neat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SerializerTest {

    @Test
    fun testSerializeSameOutputs() {
        // We expect these 2 json strings to be the same
        val neat = XorUtil.createNeat()
        val json1 = neat.serialize()
        val json2 = neat.serialize()

        assertEquals(json1, json2)
    }

    @Test
    fun testSerializeDeserialize() {
        // Starting from the same NEAT instance, then flip-flop
        val neat = XorUtil.createNeat()
        val json1 = neat.serialize()
        val deserialized = NeatImpl.fromJson(json1)
        val json2 = deserialized.serialize()

        // Since the order of species and clients can change, we can't compare the JSON strings directly
        // Instead, we parse the JSON strings into json objects (lists and maps) and compare them
        val mapper = jacksonObjectMapper()
        val obj1 = mapper.readValue(json1, Map::class.java)
        val obj2 = mapper.readValue(json2, Map::class.java)
        assertEquals(obj1, obj2)
    }

    @Test
    fun testSerializeAfterEvolution() {
        // Evolve the NEAT instance, then serialize
        val neat = XorUtil.createNeat()
        for (i in 0 until 100) {
            neat.evolve()
        }

        // Just make sure this doesn't throw an exception
        val json = neat.serialize()
        println(json)
    }

    @Test
    fun testDeserialize() {
        val json1 = javaClass.getResource("/example_neat.json")?.readText() ?: throw IllegalStateException("Could not read file")
        val neat = NeatImpl.fromJson(json1)

        // The generated JSON should be the same as the original JSON
        val json2 = neat.serialize()

        // Since the order of species and clients can change, we can't compare the JSON strings directly
        // Instead, we parse the JSON strings into json objects (lists and maps) and compare them
        val mapper = jacksonObjectMapper()
        val obj1 = mapper.readValue(json1, Map::class.java)
        val obj2 = mapper.readValue(json2, Map::class.java)
        assertEquals(obj1, obj2)
    }
}