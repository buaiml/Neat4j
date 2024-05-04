package com.cjcrafter.compute.struct

import java.nio.ByteBuffer

/**
 * Represents a struct in GLSL. This is a collection of [StructType] objects that represent
 * the fields of the struct.
 */
class Struct {
    private val fields = mutableListOf<FieldValuePair<*>>()
    private var totalSize = 0

    fun <T : Any> set(field: StructType<T>, value: T) {
        fields.add(FieldValuePair(field, value))

        val newOffset = field.calculateOffset(totalSize)

        // Add singular bytes to the buffer to align the data
        while (totalSize < newOffset) {
            totalSize++
        }
        totalSize += field.size
    }

    fun build(): ByteBuffer {
        val buffer = ByteBuffer.allocate(totalSize)

        var offset = 0
        for (field in fields) {
            val newOffset = field.type.calculateOffset(offset)

            // Add singular bytes to the buffer to align the data
            while (offset < newOffset) {
                buffer.put(0)
                offset++
            }

            field.putInBuffer(buffer)
            offset += field.type.size
        }

        buffer.flip()
        return buffer
    }

    private data class FieldValuePair<T>(val type: StructType<T>, val value: T) {
        fun putInBuffer(buffer: ByteBuffer) {
            type.putInBuffer(buffer, value)
        }
    }

    companion object {

        /**
         * Dsl function to create a [Struct] object.
         */
        fun struct(init: Struct.() -> Unit): ByteBuffer {
            val struct = Struct()
            struct.init()
            return struct.build()
        }
    }
}
