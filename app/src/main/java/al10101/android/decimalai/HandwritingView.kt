package al10101.android.decimalai

import al10101.android.decimalai.utils.VIEW_TAG
import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

class HandwritingView(context: Context): GLSurfaceView(context) {

    private val renderer = HandwritingRenderer(context)

    init {
        // Define the context as OpenGL 2.0 and renderer
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {

        // Coordinates on screen to perform the drawing
        val normalizedX = (ev.x / width) * 2f - 1f
        val normalizedY = -((ev.y / height) * 2f - 1f)

        when (ev.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_MOVE -> {
                Log.d(VIEW_TAG, "X= %6.2f  Y=%6.2f".format(normalizedX, normalizedY))
            }

            MotionEvent.ACTION_UP -> {
                Log.d(VIEW_TAG, "Handwriting stopped!")
            }

        }

        return true
    }

}