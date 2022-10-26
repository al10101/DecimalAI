package al10101.android.decimalai

import al10101.android.decimalai.utils.MODEL_TAG
import al10101.android.decimalai.utils.RENDERER_TAG
import android.opengl.GLES20.*
import android.util.Log

class FrameBufferTexture(xResolution: Int, yResolution: Int) {

    private val fbo = IntArray(1)
    private val rbo = IntArray(1)
    val texture = IntArray(1)

    init {

        // Generate the FBO
        glGenFramebuffers(1, fbo, 0)

        // Create the texture
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, xResolution, yResolution, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        // Define parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // When the texture is completed, we unbind it to prevent unpredictable behaviour
        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach texture as FBO buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture[0], 0)

        // Create render buffer for depth testing
        glGenRenderbuffers(1, rbo, 0)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo[0])
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, xResolution, yResolution)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        // Attach the render buffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rbo[0])

        // Unbind framebuffer to make sure we are not accidentally rendering to the wrong buffer
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Log.e(RENDERER_TAG, "FRAMEBUFFER: Framebuffer is not complete!")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    fun useFBO(width: Int, height: Int) {

        // Bind so the next gl calls write into this buffer
        glViewport(0, 0, width, height)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

    }

}