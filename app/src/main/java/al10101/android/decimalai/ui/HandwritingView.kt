package al10101.android.decimalai.ui

import al10101.android.decimalai.utils.VIEW_TAG
import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class HandwritingView(context: Context): GLSurfaceView(context) {

    private var digit: Int? = null
    private var cert = 0f

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

                // Since the OpenGL thread runs async to the main thread, we should define an special
                // runnable to retrieve the digit and certainty variables that are computed within the
                // renderer
                val task: FutureTask<FloatArray> = FutureTask {
                    return@FutureTask renderer.onStop()
                }

                // Now we simply perform the task and ask for the values
                queueEvent(task)
                val digitCert = task.get()

                // Unroll the variables. Remember that we used the -1 as a key if the actual digit
                // is null
                digit = if (digitCert[0] < 0f) { null } else { digitCert[0].toInt() }
                cert = digitCert[1]

                // Pass this information to the fragment that contains the UI text. It will be
                // updated at the same time because it uses live data and an observable for each value
                UIFragment.digitLiveData.value = digit
                UIFragment.certainLiveData.value = cert

            }

        }

        return true

    }

}