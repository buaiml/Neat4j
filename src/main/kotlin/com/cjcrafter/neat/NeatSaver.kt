package com.cjcrafter.neat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

/**
 * Saves the state of a NEAT instance to disk.
 *
 * This can be used to save the state of the NEAT instance at a certain point in
 * time. This is useful for saving the state of the NEAT instance after a certain
 * number of generations, or when the NEAT instance has found a solution.
 *
 * @property neat The NEAT instance to save.
 * @property saveFolder The folder to save the NEAT instance to.
 */
class NeatSaver(
    val neat: Neat,
    var saveFolder: File,
) {
    fun save() {
        val neatJson = neat.serialize()
        val neatFile = File(saveFolder, "generation-${neat.generationNumber}.json")
        neatFile.writeText(neatJson)

        val bestClientFile = File(saveFolder, "best-client-${neat.generationNumber}.json")
        val bestClient = neat.clients.maxByOrNull { it.score }!!
        val mapper = jacksonObjectMapper()
        val bestClientJson = mapper.writeValueAsString(bestClient)
        bestClientFile.writeText(bestClientJson)
    }
}