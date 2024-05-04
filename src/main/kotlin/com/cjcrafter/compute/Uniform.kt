package com.cjcrafter.compute

import org.lwjgl.opengl.GL43C.*

/**
 * Provides methods to set
 */
interface Uniform {
    val programId: Int

    fun setFloat(uniformLocation: Int, value: Float) {
        glUniform1f(uniformLocation, value)
    }

    fun setFloats(uniformLocation: Int, value: FloatArray) {
        glUniform1fv(uniformLocation, value)
    }
}