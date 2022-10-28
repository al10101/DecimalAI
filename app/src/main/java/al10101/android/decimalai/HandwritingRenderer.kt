package al10101.android.decimalai

import al10101.android.decimalai.utils.NANOSECONDS
import al10101.android.decimalai.utils.RENDERER_TAG
import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import android.util.Log
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

const val DEBUGGING = false

class HandwritingRenderer(private val context: Context): GLSurfaceView.Renderer {

    private lateinit var nn: NeuralNetwork

    private val projectionMatrix = FloatArray(16)
    private val inverseProjectionMatrix = FloatArray(16)
    private val touchVector = FloatArray(4)

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
    private val timeBeforeClean = 2f // seconds
    private var timeSinceLastTouch = 0f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Set background color to a light gray
        val grayScale = 0.1f
        glClearColor(grayScale, grayScale, grayScale, 1f)

        canvasProgram = CanvasShaderProgram(context)
        debugProgram = DebugShaderProgram(context)

        // Quad for debugging in NDC space to occupy the whole screen (from -1 to 1 equals 2 for each axis)
        quad = Quad(2f, 2f)

        // We define the canvas to occupy the whole screen AFTER the orthographic projection is applied
        val black = floatArrayOf(0f, 0f, 0f, 1f)
        val slicesPerAxis = 60
        canvas = Grid(2f, 2f, black, slicesPerAxis, slicesPerAxis)

        // We can define the NN here already
        nn = NeuralNetwork(context)

        globalStartTime = System.nanoTime()

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        // Save the resolution variables to use later after the FBO rendering
        screenWidth = width
        screenHeight = height
        frameBufferTexture = FrameBufferTexture(width, height)

        Log.i(RENDERER_TAG, "Dims: W= $width  H= $height")

        // Set orthographic projection so the canvas object is clamped to the whole screen
        landscape = width > height
        ratio = if (landscape) {
            width.toFloat() / height.toFloat()
        } else {
            height.toFloat() / width.toFloat()
        }

        setProjection()

    }

    private fun setProjection() {
        if (landscape) {
            orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
        }
        invertM(inverseProjectionMatrix, 0, projectionMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {

        // Update timer
        currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS

        // Clear the canvas
        glClear(GL_COLOR_BUFFER_BIT)

        // Only draw the debug mode for a fixed amount of time after the user released the screen
        if (currentTime - timeSinceLastTouch > timeBeforeClean && DEBUGGING) {
            debugProgram.useProgram()
            debugProgram.setUniforms(frameBufferTexture.texture[0])
            quad.bindData(debugProgram)
            quad.draw()
        }

        renderCanvas()

    }

    private fun renderCanvas() {
        canvasProgram.useProgram()
        canvasProgram.setUniforms(projectionMatrix)
        canvas.bindData(canvasProgram)
        canvas.draw()
    }

    fun onTouch(normalizedX: Float, normalizedY: Float) {

        // Restart canvas if there has passed enough time since the last time this function was called
        if (currentTime - timeSinceLastTouch > timeBeforeClean) {
            canvas.resetColors()
        }

        // Update timer
        timeSinceLastTouch = currentTime

        // Since the normalized touch occurs in NDC just like the canvas, we must invert the coordinates to
        // render the correct shape in real time
        val normalizedVector = floatArrayOf(normalizedX, normalizedY, 0f, 1f)
        multiplyMV(touchVector, 0, inverseProjectionMatrix, 0, normalizedVector, 0)

        // When the user touches the canvas, the corresponding point must be recolored
        val lightWhite = 1f
        val drawingColor = floatArrayOf(lightWhite, lightWhite, lightWhite, 1f)
        canvas.updateColor(touchVector[0], touchVector[1], drawingColor)

    }

    fun onStop() {

        // Prepare the fbo to render with the correct number of pixels that the NN needs
        frameBufferTexture.useFBO(nn.xPixels, nn.yPixels)

        // As a little "trick" to ensure that the canvas will occupy the whole screen in the
        // fbo, we forget about projection for this one render
        setIdentityM(projectionMatrix, 0)
        renderCanvas()

        Log.i(RENDERER_TAG, "Scene written to the FBO successfully")

        // Process the fbo values of the whole screen
        val x = pixelsPreprocessing(nn.xPixels, nn.yPixels)

        Log.i(RENDERER_TAG, "All ${x.size} values prepared to be used by the NN")

        // Compute prediction
        val h = nn.forward(x)

        Log.i(RENDERER_TAG, "Probabilities: ${h.contentToString()}")

        // Now we return to original projection and reset the frame buffer for the next frame
        setProjection()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, screenWidth, screenHeight)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)

    }

    private fun pixelsPreprocessing(width: Int, height: Int): FloatArray {

        val totalSize = nn.inputs

        // For reasons which will become clearer when transforming to grayscale, we only need 1 component
        // from the RGBA components, so we will define the array as such: to store 1 integer per pixel
        val b = IntArray(totalSize)
        val pixelsRGB = IntBuffer.wrap(b)
        pixelsRGB.position(0)

        glPixelStorei(GL_PACK_ALIGNMENT, 1)
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixelsRGB)

        // Save all colors to a float array as grayscale values
        val pixelsGray = FloatArray(totalSize)
        for (i in 0 until totalSize) {
            // Since the gray scale is good already and we defined the pixelsRGB to be just with 1 component,
            // we will read the value as if it were a red component. This way is easier to code, faster to
            // compute and without any major repercussion to the global behaviour of the algorithm
            pixelsGray[i] = Color.red(pixelsRGB[i]) / 255f
        }

        var temp: Float

        // Ok, this next part is a little bit tricky. By now, we have all the pixels from low to high
        // rows. We need to reverse all those rows so the number is actually read correctly
        for (i in 0 until width) {
            for (j in 0 until height / 2) { // Only half the values so we don't end up with the same result
                val ij = j * height + i
                val ijj = (height - j - 1) * height + i
                temp = pixelsGray[ijj]
                pixelsGray[ijj] = pixelsGray[ij]
                pixelsGray[ij] = temp
            }
        }

        // As a final step to match the user handwritten digit to the NN input form, we take the
        // transpose as if the pixels were a 20x20 matrix
        for (i in 0 until width) {
            for (j in 0 until i) { // Only the first diagonal so we don't end up with the same result
                val ij = j * height + i
                val ji = i * width + j
                temp = pixelsGray[ji]
                pixelsGray[ji] = pixelsGray[ij]
                pixelsGray[ij] = temp
            }
        }

        return pixelsGray

    }

}