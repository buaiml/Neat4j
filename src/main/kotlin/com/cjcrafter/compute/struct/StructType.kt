package com.cjcrafter.compute.struct

import org.joml.Matrix2dc
import org.joml.Matrix2fc
import org.joml.Matrix3fc
import org.joml.Vector2d
import org.joml.Vector2dc
import org.joml.Vector2fc
import org.joml.Vector2ic
import org.joml.Vector3dc
import org.joml.Vector3fc
import org.joml.Vector3ic
import org.joml.Vector4dc
import org.joml.Vector4fc
import org.joml.Vector4ic
import java.nio.ByteBuffer

/**
 * Represents the type of 1 variable in a [Struct]. For more information on types,
 * see [OpenGL Data Types](https://www.khronos.org/opengl/wiki/Data_Type_(GLSL)).
 *
 * This follows the GLSL std140 alignment rules, so:
 * * Scalars are aligned to 4 bytes
 *
 * Recall that the stride (or size) of the data stored is not the same as the alignment of the
 * data (alignment >= stride). The stride is the size of the data stored in the buffer, while the
 * alignment is the number of bytes the data is aligned to.
 */
abstract class StructType<T>(val size: Int, val alignment: Int, val type: Class<T>) {

    fun calculateOffset(currentOffset: Int): Int {
        val alignmentMask = alignment - 1
        return (currentOffset + alignmentMask) and alignmentMask.inv()
    }

    abstract fun putInBuffer(buffer: ByteBuffer, value: T)

    companion object {
        private inline fun <reified T> of(
            size: Int,
            alignment: Int = size,
            crossinline putAction: (ByteBuffer, T) -> Unit
        ): StructType<T> {
            return object : StructType<T>(size, alignment, T::class.java) {
                override fun putInBuffer(buffer: ByteBuffer, value: T) {
                    putAction(buffer, value)
                }
            }
        }

        // Scalar values
        @JvmStatic val BOOL: StructType<Boolean> = of(4, 4) { buffer, value -> buffer.putInt(if (value) 1 else 0) }
        @JvmStatic val INT: StructType<Int> = of(4, 4, ByteBuffer::putInt)
        @JvmStatic val UINT: StructType<UInt> = of(4, 4) { buffer, value -> buffer.putInt(value.toInt()) }
        @JvmStatic val FLOAT: StructType<Float> = of(4, 4, ByteBuffer::putFloat)
        @JvmStatic val DOUBLE: StructType<Double> = of(8, 8, ByteBuffer::putDouble)

        // Vector values
        // TODO bool, uint
        @JvmStatic val IVEC2: StructType<Vector2ic> = of(INT.size * 2) { buffer, value ->
            buffer.putInt(value.x())
            buffer.putInt(value.y())
        }
        @JvmStatic val VEC2: StructType<Vector2fc> = of(FLOAT.size * 2) { buffer, value ->
            buffer.putFloat(value.x())
            buffer.putFloat(value.y())
        }
        @JvmStatic val DVEC2: StructType<Vector2dc> = of(DOUBLE.size * 2) { buffer, value ->
            buffer.putDouble(value.x())
            buffer.putDouble(value.y())
        }

        @JvmStatic val IVEC3: StructType<Vector3ic> = of(INT.size * 3, INT.size * 4) { buffer, value ->
            buffer.putInt(value.x())
            buffer.putInt(value.y())
            buffer.putInt(value.z())
        }
        @JvmStatic val VEC3: StructType<Vector3fc> = of(FLOAT.size * 3, FLOAT.size * 4) { buffer, value ->
            buffer.putFloat(value.x())
            buffer.putFloat(value.y())
            buffer.putFloat(value.z())
        }
        @JvmStatic val DVEC3: StructType<Vector3dc> = of(DOUBLE.size * 3, DOUBLE.size * 4) { buffer, value ->
            buffer.putDouble(value.x())
            buffer.putDouble(value.y())
            buffer.putDouble(value.z())
        }

        @JvmStatic val IVEC4: StructType<Vector4ic> = of(INT.size * 4) { buffer, value ->
            buffer.putInt(value.x())
            buffer.putInt(value.y())
            buffer.putInt(value.z())
            buffer.putInt(value.w())
        }
        @JvmStatic val VEC4: StructType<Vector4fc> = of(FLOAT.size * 4) { buffer, value ->
            buffer.putFloat(value.x())
            buffer.putFloat(value.y())
            buffer.putFloat(value.z())
            buffer.putFloat(value.w())
        }
        @JvmStatic val DVEC4: StructType<Vector4dc> = of(DOUBLE.size * 4) { buffer, value ->
            buffer.putDouble(value.x())
            buffer.putDouble(value.y())
            buffer.putDouble(value.z())
            buffer.putDouble(value.w())
        }

        // Square Matrix values
        @JvmStatic val MAT2: StructType<Matrix2fc> = of(FLOAT.size * 4) { buffer, value ->
            buffer.putFloat(value.m00())
            buffer.putFloat(value.m01())
            buffer.putFloat(value.m10())
            buffer.putFloat(value.m11())
        }
        @JvmStatic val DMAT2: StructType<Matrix2dc> = of(DOUBLE.size * 4) { buffer, value ->
            buffer.putDouble(value.m00())
            buffer.putDouble(value.m01())
            buffer.putDouble(value.m10())
            buffer.putDouble(value.m11())
        }

        // TODO check alignment
        @JvmStatic val MAT3: StructType<Matrix3fc> = of(FLOAT.size * 9) { buffer, value ->
            buffer.putFloat(value.m00())
            buffer.putFloat(value.m01())
            buffer.putFloat(value.m02())
            buffer.putFloat(value.m10())
            buffer.putFloat(value.m11())
            buffer.putFloat(value.m12())
            buffer.putFloat(value.m20())
            buffer.putFloat(value.m21())
            buffer.putFloat(value.m22())
        }
    }
}