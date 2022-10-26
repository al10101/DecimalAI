package al10101.android.decimalai

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HandwritingRenderer(context: Context): GLSurfaceView.Renderer {

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Set background color
        glClearColor(1f, 0f, 0f, 1f)

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {

        // Clear the canvas
        glClear(GL_COLOR_BUFFER_BIT)

    }

}