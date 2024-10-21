package com.cjcrafter.neat

import com.cjcrafter.neat.compute.Calculator
import com.cjcrafter.neat.compute.SimpleCalculator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class SerializerTest {

    @Test
    fun test_serializeSameOutputs() {
        // We expect these 2 json strings to be the same
        val neat = XorUtil.createNeat()
        val json1 = neat.serialize()
        val json2 = neat.serialize()

        assertEquals(json1, json2)
    }

    @Test
    fun test_serializeDeserialize() {
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
    fun test_serializeAfterEvolution() {
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
    fun test_deserializeNeat() {
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

    @Test
    fun test_deserializeCalculator() {
        val json1 = javaClass.getResource("/example_calculator.json")?.readText() ?: throw IllegalStateException("Could not read file")
        val calculator = SimpleCalculator.fromJson(json1)

        // We should be able to call this method
        val results: FloatArray = calculator.calculate(floatArrayOf(1f, 0f)).join()

        // The generated JSON should be the same as the original JSON
        val json2 = jacksonObjectMapper().writeValueAsString(calculator)

        val mapper = jacksonObjectMapper()
        val obj1 = mapper.readValue(json1, Map::class.java)
        val obj2 = mapper.readValue(json2, Map::class.java)
        assertEquals(obj1, obj2)
    }

    @Test
    fun test_neatSaver() {
        val neat = XorUtil.createNeat()
        val saver = NeatSaver(neat, File("serialize"))

        // Clean up the folder
        saver.saveFolder.deleteRecursively()
        saver.saveFolder.mkdirs()

        // Try to save each generation to file
        saver.save()
        for (i in 0 until 2) {
            neat.evolve()
            saver.save()
        }

        // Clean up the folder again
        //saver.saveFolder.deleteRecursively()
    }
}