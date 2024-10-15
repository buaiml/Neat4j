package com.cjcrafter.neat

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SerializerTest {

    @Test
    fun testSerializeSameOutputs() {
        // We expect these 2 json strings to be the same
        val json1 = XorUtil.createNeat().serialize()
        val json2 = XorUtil.createNeat().serialize()

        assertEquals(json1, json2)
    }

    @Test
    fun testSerializeDeserialize() {
        // Starting from the same NEAT instance, then flip-flop
        val neat = XorUtil.createNeat()
        val json1 = neat.serialize()
        val deserialized = NeatImpl.fromJson(json1)
        val json2 = deserialized.serialize()

        assertEquals(json1, json2)
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
}