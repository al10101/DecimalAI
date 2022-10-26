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

    private val viewProjectionMatrix = FloatArray(16)

    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var frameBufferTexture: FrameBufferTexture

    private var landscape = false
    private var ratio = 0f

    private lateinit var canvas: Grid
    private lateinit var quad: Quad

    private lateinit var canvasProgram: CanvasShaderProgram
    private lateinit var debugProgram: DebugShaderProgram

    private var globalStartTime: Long = 0
    private var currentTime = 0f
    private val timeBeforeRestart = 2f
    private var timeSinceLastTouch = 0f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Set background color to a light gray
        val grayScale = 0.1f
        glClearColor(grayScale, grayScale, grayScale, 1f)

        canvasProgram = CanvasShaderProgram(context)
        debugProgram = DebugShaderProgram(context)

        // Quad for debugging in NDC space to occupy the whole screen (from -1 to 1 equals 2 for each axis)
        quad = Quad(2f, 2f)

        globalStartTime = System.nanoTime()

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        // Save the resolution variables to use later in the FBO
        screenWidth = width
        screenHeight = height
        frameBufferTexture = FrameBufferTexture(width, height)

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

        // Only draw the debug mode for a fixed amount of time after the user released the screen
        if (currentTime - timeSinceLastTouch > timeBeforeRestart) {
            debugProgram.useProgram()
            debugProgram.setUniforms(frameBufferTexture.texture[0])
            quad.bindData(debugProgram)
            quad.draw()
        } else {
            renderCanvas()
        }

    }

    private fun renderCanvas() {
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

        // Render the scene to the framebuffer. We define the fbo with the dimensions that will
        // be used for the NN input (20x20 pixels)
        frameBufferTexture.useFBO(20, 20)
        renderCanvas()
        Log.i(RENDERER_TAG, "Scene written to the FBO successfully")

        // Reset state
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, screenWidth, screenHeight)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)

    }

}