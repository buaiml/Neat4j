package com.cjcrafter.compute.struct

import com.cjcrafter.compute.struct.Struct.Companion.struct
import org.joml.Vector2f
import org.joml.Vector3f
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests the [Struct] class by creating structs with various types and values, then examining the
 * resulting byte buffer's binary data.
 */
class StructTest {

    @Test
    fun testBooleans() {
        // booleans have weird alignment; each boolean takes up 4 bytes in glsl

        val byteBuffer = struct {
            set(StructType.BOOL, true)
            set(StructType.BOOL, true)
            set(StructType.BOOL, true)
            set(StructType.BOOL, true)
        }

        // Make sure the byte buffer has been flipped
        assertEquals(0, byteBuffer.position())
        assertEquals(16, byteBuffer.limit())

        // We should be able to read exactly 4 integers from the buffer, each of which should be 1
        assertEquals(1, byteBuffer.int)
        assertEquals(1, byteBuffer.int)
        assertEquals(1, byteBuffer.int)
        assertEquals(1, byteBuffer.int)

        // Make sure the buffer is empty
        assertEquals(16, byteBuffer.position())
    }

    @Test
    fun testVec3() {
        // vec3 only takes up 12 bytes, but the alignment is 16 bytes. So a buffer with ONLY a vec3
        // should have 12 bytes of data.

        val byteBuffer = struct {
            set(StructType.VEC3, Vector3f(1.0f, 2.0f, 3.0f))
        }

        // Make sure the byte buffer has been flipped
        assertEquals(0, byteBuffer.position())
        assertEquals(12, byteBuffer.limit())

        // We should be able to read exactly 3 floats from the buffer, each of which should be 1, 2, and 3
        assertEquals(1.0f, byteBuffer.float)
        assertEquals(2.0f, byteBuffer.float)
        assertEquals(3.0f, byteBuffer.float)

        // Make sure the buffer is empty
        assertEquals(12, byteBuffer.position())
    }

    @Test
    fun testCompactVec3() {
        // A buffer with a vec3 and a float should have 16 bytes of data.

        val byteBuffer = struct {
            set(StructType.VEC3, Vector3f(1.0f, 2.0f, 3.0f))
            set(StructType.FLOAT, 4.0f)
        }

        // Make sure the byte buffer has been flipped
        assertEquals(0, byteBuffer.position())
        assertEquals(16, byteBuffer.limit())

        // We should be able to read exactly 4 floats from the buffer, each of which should be 1, 2, 3, and 4
        assertEquals(1.0f, byteBuffer.float)
        assertEquals(2.0f, byteBuffer.float)
        assertEquals(3.0f, byteBuffer.float)
        assertEquals(4.0f, byteBuffer.float)

        // Make sure the buffer is empty
        assertEquals(16, byteBuffer.position())
    }

    @Test
    fun testWasteVec3() {
        // A buffer with a float, then a vec3 should have 28 bytes of data (since the first float
        // will take up 16 bytes, due to the poor alignment)

        val byteBuffer = struct {
            set(StructType.FLOAT, 1.0f)
            set(StructType.VEC3, Vector3f(2.0f, 3.0f, 4.0f))
        }

        // Make sure the byte buffer has been flipped
        assertEquals(0, byteBuffer.position())
        assertEquals(28, byteBuffer.limit())

        // We should be able to read exactly 4 floats from the buffer, each of which should be 1, 0, 0, 0
        assertEquals(1.0f, byteBuffer.float)
        byteBuffer.float // ignore
        byteBuffer.float // ignore
        byteBuffer.float // ignore
        assertEquals(2.0f, byteBuffer.float)
        assertEquals(3.0f, byteBuffer.float)
        assertEquals(4.0f, byteBuffer.float)

        // Make sure the buffer is empty
        assertEquals(28, byteBuffer.position())
    }

    @Test
    fun testVec3WithPadding() {
        // A buffer with a vec3 and a vec3 should have 28 bytes of data (NOT 24... alignment!!).

        val byteBuffer = struct {
            set(StructType.VEC3, Vector3f(1.0f, 2.0f, 3.0f))
            set(StructType.VEC3, Vector3f(4.0f, 5.0f, 6.0f))
        }

        // Make sure the byte buffer has been flipped
        assertEquals(0, byteBuffer.position())
        assertEquals(28, byteBuffer.limit())

        // We should be able to read exactly 6 floats from the buffer, each of which should be 1, 2, 3, 4, 5, and 6
        assertEquals(1.0f, byteBuffer.float)
        assertEquals(2.0f, byteBuffer.float)
        assertEquals(3.0f, byteBuffer.float)
        byteBuffer.float // ignore
        assertEquals(4.0f, byteBuffer.float)
        assertEquals(5.0f, byteBuffer.float)
        assertEquals(6.0f, byteBuffer.float)

        // Make sure the buffer is empty
        assertEquals(28, byteBuffer.position())
    }
}