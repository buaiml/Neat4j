package com.cjcrafter.neat.performance

import com.cjcrafter.neat.NeatPrinter
import com.cjcrafter.neat.XorUtil
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.concurrent.Executors
import kotlin.test.Test

/**
 * The original NEAT paper claims to solve the XOR problem in 32 generations
 * (averaged over 100 trials). Solutions averaged 2.35 hidden nodes and 7.48
 * connection genes.
 *
 * Over 100 trials, the algorithm should always solve the XOR problem in less
 * than 100 generations.
 */
class XorPerformance {

    @Test
    fun runExperiment() = runBlocking {
        testXorPerformance()
    }

    suspend fun testXorPerformance() = coroutineScope {
        val trials = 1000
        val channel = Channel<Int>()
        val results = mutableListOf<Int>()

        val saveFolder = File("xor-performance")
        saveFolder.deleteRecursively()

        val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()

        repeat(trials) {
            launch(dispatcher) {
                val generations = testXorOnce(it)
                channel.send(generations)
            }
        }

        repeat(trials) {
            results.add(channel.receive())
        }

        results.sort()
        val average = results.average()
        val min = results.min()
        val max = results.max()

        println("Average generations: $average")
        println("Min generations: $min")
        println("Max generations: $max")

        // Show the results as a graph, so we can see the distribution
        val histogram = results.groupingBy { it }.eachCount()
        histogram.forEach { (key, value) ->
            println("$key: ${"*".repeat(value)}")
        }
    }

    fun testXorOnce(trial: Int): Int {
        val neat = XorUtil.createNeat()
        val debug = NeatPrinter(neat)
        val saveFolder = File("xor-performance")
        saveFolder.mkdirs()
        val saveFile = File(saveFolder, "xor-performance-$trial.txt")

        var generation = 0
        while (!XorUtil.score(neat) && generation < 2500) {
            generation++

            // When the generation exceeds the norm, we should save debug info
            // to file.
            if (generation > 60) {
                val info = debug.render()
                saveFile.appendText(info.toString() + "\n", Charset.defaultCharset())
            }

            neat.evolve()
        }

        if (generation > 60) {
            println("Trial #$trial took $generation generations")
        }

        return generation
    }
}