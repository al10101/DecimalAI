package al10101.android.decimalai

import al10101.android.decimalai.utils.VIEW_TAG
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import java.io.*

class HandwritingView(context: Context): GLSurfaceView(context) {

    private val renderer = HandwritingRenderer(context)

    init {
        // Define the context as OpenGL 2.0 and renderer
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {

        // NDC computed from the touch event
        val normalizedX = (ev.x / width) * 2f - 1f
        val normalizedY = -((ev.y / height) * 2f - 1f)

        when (ev.action and MotionEvent.ACTION_MASK) {

            // Either if it is the first touch or a continuous drag, perform the same action: draw
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                Log.d(VIEW_TAG, "Touch Event: X= %8.4f  Y= %8.4f".format(normalizedX, normalizedY))
                queueEvent {
                    renderer.onTouch(normalizedX, normalizedY)
                }
            }

            MotionEvent.ACTION_UP -> {
                Log.d(VIEW_TAG, "Handwriting stopped!")
                queueEvent {
                    renderer.onStop()
                }
            }

        }

        return true

    }

}