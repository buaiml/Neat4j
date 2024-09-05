package com.cjcrafter.neat

import kotlin.math.abs

object XorUtil {

    val XOR_INPUTS = listOf(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(1f, 0f, 1f),
        floatArrayOf(1f, 1f, 0f),
        floatArrayOf(1f, 1f, 1f),
    )

    val XOR_OUTPUTS = floatArrayOf(
        0f,
        1f,
        1f,
        0f,
    )

    fun createNeat(): Neat {
        // 2 inputs + 1 bias
        // 1 output
        val parameters = Parameters()
        parameters.useBiasNode = false  // use our own bias node
        val neat = NeatImpl(3, 1, 150, parameters)
        return neat
    }

    fun score(neat: Neat): Boolean {
        var anySolved = false
        for (client in neat.clients) {
            client.score = 4.0
            var solved = true

            for (i in 0 until 4) {
                val inputs = XOR_INPUTS[i]
                val expected = XOR_OUTPUTS[i]
                val actual = client.calculator.calculate(inputs).join()[0]

                val normalized = if (actual > 0.5) 1f else 0f
                if (normalized != expected) {
                    solved = false
                }

                val diff = abs(expected - actual)
                client.score -= diff * diff
            }

            anySolved = anySolved || solved
        }

        return anySolved
    }

}