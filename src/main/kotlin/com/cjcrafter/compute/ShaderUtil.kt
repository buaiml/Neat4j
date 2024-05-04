package com.cjcrafter.compute

import org.lwjgl.opengl.GL43C.*

object ShaderUtil {

    fun createTexture(width: Int, height: Int, imageType: Int = GL_R32F): Int {
        val tex = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, tex)
        glTexStorage2D(GL_TEXTURE_2D, 1, imageType, width, height)

        // cleanup
        glBindTexture(GL_TEXTURE_2D, 0)
        return tex
    }

}