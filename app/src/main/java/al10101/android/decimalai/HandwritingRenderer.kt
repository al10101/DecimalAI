package al10101.android.decimalai

import al10101.android.decimalai.utils.NANOSECONDS
import al10101.android.decimalai.utils.RENDERER_TAG
import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HandwritingRenderer(private val context: Context): GLSurfaceView.Renderer {

    private var landscape = false
    private var ratio = 0f

    private lateinit var canvas: Grid
    private lateinit var canvasProgram: CanvasShaderProgram

    private var globalStartTime: Long = 0
    private var currentTime = 0f
    private val timeBeforeRestart = 2f
    private var timeSinceLastTouch = 0f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Set background color
        glClearColor(0.1f, 0.1f, 0.1f, 1f)

        val vertexShader = R.raw.vertex_shader
        val fragmentShader = R.raw.fragment_shader
        canvasProgram = CanvasShaderProgram(context,
            vertexShader,
            fragmentShader
        )

        globalStartTime = System.nanoTime()

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        // For reasons which will become clear later, we will use NDC space (instead of world space) and
        // perform the corresponding operations by hand. That is, we will define the canvas as a
        // square in NDC space.

        // First we need to know if the screen's orientation is set vertically (portrait) or horizontally (landscape).
        // We will use the ratio for that
        landscape = width > height
        ratio = if (landscape) {
            width.toFloat() / height.toFloat()
        } else {
            height.toFloat() / width.toFloat()
        }

        Log.i(RENDERER_TAG, "Landscape? $landscape")
        Log.i(RENDERER_TAG, "Ratio= %.4f".format(ratio))

        // Since it is a square, we will set the quad's dimensions the same: 2f -> from -1 to +1
        var qWidth = 2f
        var qHeight = 2f

        // We divide by the ratio to correct the proportions of the quad
        if (landscape) {
            qWidth /= ratio
        } else {
            qHeight /= ratio
        }

        Log.i(RENDERER_TAG, "Dims: W= %.4f  Y= %.4f".format(qWidth, qHeight))

        // Finally, we define the quad with the corrected dimensions
        val black = floatArrayOf(0f, 0f, 0f, 1f)
        val slicesPerAxis = 60
        canvas = Grid(qWidth, qHeight, black, slicesPerAxis, slicesPerAxis)

    }

    override fun onDrawFrame(p0: GL10?) {

        // Update timer
        currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS

        // Clear the canvas
        glClear(GL_COLOR_BUFFER_BIT)

        // Draw the canvas
        canvasProgram.useProgram()
        // No uniforms for this shader
        canvas.bindData(canvasProgram)
        canvas.draw()

    }

    fun onTouch(normalizedX: Float, normalizedY: Float) {

        // Restart canvas if there has passed enough time since the last time this function was called
        if (currentTime - timeSinceLastTouch > timeBeforeRestart) {
            canvas.resetColors()
        }

        // Update timer
        timeSinceLastTouch = currentTime

        // When the user touches the canvas, the corresponding point must be recolored
        val white = floatArrayOf(1f, 1f, 1f, 1f)
        canvas.updateColor(landscape, ratio, normalizedX, normalizedY, white)

    }

    fun onStop() {

        // TODO: Predict Number with NN

    }

}