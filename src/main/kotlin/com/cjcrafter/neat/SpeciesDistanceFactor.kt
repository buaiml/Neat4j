package com.cjcrafter.neat

import kotlin.math.abs

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

        // When switching directions, reset velocity
        val target: Float
        if (delta > 0) {
            target = min
            if (velocity > 0)
                velocity = 0f
        } else {
            target = max
            if (velocity < 0)
                velocity = 0f
        }

        // determine how fast to move towards target (if we have WAY off, we
        // should try to move faster... but cap at 0.25 per generation)
        val deltaTime = 0.004f * abs(delta)

        speciesDistance = smoothDamp(
            speciesDistance,
            target,
            0.008f * neat.parameters.targetSpeciesCount,
            deltaTime,
            0.25f / deltaTime,
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