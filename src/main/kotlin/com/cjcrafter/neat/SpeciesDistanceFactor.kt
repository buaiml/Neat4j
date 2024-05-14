package com.cjcrafter.neat

import kotlin.jvm.internal.Ref.FloatRef
import kotlin.math.abs
import kotlin.math.pow

class SpeciesDistanceFactor(
    override val neat: Neat,
    var speciesDistance: Float = 3.0f,
) : NeatInstance {

    var velocity = 0f

    fun update() {
        val min = 0.1f
        val max = neat.parameters.speciesDistance * 2.0f

        val delta = neat.parameters.targetSpeciesCount - neat.allSpecies.size
        if (delta == 0)
            return

        val target: Float
        if (delta > 0) {
            target = min
        } else {
            target = max
        }

        speciesDistance = smoothDamp(
            speciesDistance,
            target,
            0.1f,
            0.01f,
        )
    }

    private fun smoothDamp(
        current: Float,
        target: Float,
        smoothTime: Float,
        deltaTime: Float = 1.0f,
        maxSpeed: Float = Float.POSITIVE_INFINITY,
    ): Float {
        val omega = 2f / smoothTime
        val x = omega * deltaTime
        val exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x)

        val change = current - target

        // Clamp maximum speed
        val maxChange = maxSpeed * smoothTime
        val clampedChange = change.coerceIn(-maxChange, maxChange)
        val newTarget = current - clampedChange

        val temp = (velocity + omega * clampedChange) * deltaTime
        var newVelocity = (velocity - omega * temp) * exp
        var newPosition = newTarget + (clampedChange + temp) * exp

        // Prevent overshooting
        if ((target - current > 0f) == (newPosition > target)) {
            newPosition = target
            newVelocity = 0f
        }

        velocity = newVelocity
        return newPosition
    }
}