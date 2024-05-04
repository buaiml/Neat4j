package com.cjcrafter.compute

import org.joml.Vector3i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL43C.*
import java.io.InputStream
import java.nio.*

class ComputeShader(private val sourceStream: InputStream): Uniform {
    override var programId: Int = createAndCompileShader()

    private fun createAndCompileShader(): Int {
        // Create a new program
        val program = glCreateProgram()
        val shader = glCreateShader(GL_COMPUTE_SHADER)

        // Read the source code from the stream, and compile
        val shaderSource: String = sourceStream.bufferedReader().readText()
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw IllegalStateException("Shader compilation failed: ${glGetShaderInfoLog(shader)}")
        }

        // Link the shader to our "program"
        glAttachShader(program, shader)
        glLinkProgram(program)
        glDeleteShader(shader)
        return program
    }

    fun getWorkGroupSize(): Vector3i {
        val buffer = BufferUtils.createIntBuffer(3)
        glGetProgramiv(programId, GL_MAX_COMPUTE_WORK_GROUP_SIZE, buffer)
        val vector = Vector3i()
        vector.set(buffer)
        return vector
    }

    @JvmOverloads
    fun dispatch(x: Int, y: Int = 1, z: Int = 1, buffers: ComputeShader.() -> Unit = {}) {
        glUseProgram(programId)
        buffers()
        glDispatchCompute(x, y, z)
        //glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT)
        glMemoryBarrier(GL_ALL_BARRIER_BITS)
    }

    fun getUniformLocation(name: String): Int {
        return glGetUniformLocation(programId, name)
    }

    fun setBuffer(bindingIndex: Int, buffer: ByteBuffer) {
        val bufferId = glGenBuffers()
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId)
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, bindingIndex, bufferId)
    }

    fun readBuffer(bindingIndex: Int): ByteBuffer {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bindingIndex)
        val result = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_ONLY)!!

        // Copy the buffer to a new ByteBuffer
        val buffer = ByteBuffer.allocateDirect(result.remaining())
        buffer.put(result)
        buffer.flip()

        glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)
        return buffer
    }

    fun release() {
        glDeleteProgram(programId)
        glDeleteShader(programId)
    }
}