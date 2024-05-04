package com.cjcrafter.compute

import org.joml.Matrix3f
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.glfw.GLFW.glfwCreateWindow
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ComputeShaderTest {

    @Test
    fun testMatrixDoubler() {
        // Initialize resources
        val shaderCode = ComputeShaderTest::class.java.classLoader.getResourceAsStream("compute/matrix_doubler_test.glsl")!!
        val computeShader = ComputeShader(shaderCode)

        // Create a Matrix3f and fill with initial values
        val initialMatrix = Matrix3f(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f
        )
        val inputBuffer = ByteBuffer.allocateDirect(36).order(ByteOrder.nativeOrder())
        initialMatrix.get(inputBuffer)

        println(initialMatrix)
        println()

        computeShader.dispatch(3, 3) {
            setBuffer(0, inputBuffer)

            // create byte buffer of zeros for output
            val outputBuffer = ByteBuffer.allocateDirect(36).order(ByteOrder.nativeOrder())
            // fill with zeros
            for (i in 0 until 36) {
                outputBuffer.put(0)
            }
            outputBuffer.flip()

            setBuffer(1, outputBuffer)
        }

        // Read output and print results
        val outputBuffer = computeShader.readBuffer(1)
        val resultMatrix = Matrix3f()
        resultMatrix.set(outputBuffer)
        println(resultMatrix)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // Initialize OpenGL
            // Initialize LWJGL and OpenGL
            GLFWErrorCallback.createPrint(System.err).set()
            if (!glfwInit()) {
                throw IllegalStateException("Unable to initialize GLFW")
            }

            // Create a dummy window (required by LWJGL for OpenGL context)
            glfwDefaultWindowHints()
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
            val window = glfwCreateWindow(1, 1, "Compute Shader Example", 0, 0)
            glfwMakeContextCurrent(window)
            GL.createCapabilities()
        }
    }
}