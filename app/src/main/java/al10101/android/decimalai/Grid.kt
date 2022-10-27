package al10101.android.decimalai

import al10101.android.decimalai.utils.BYTES_PER_FLOAT
import al10101.android.decimalai.utils.VertexArray
import android.opengl.GLES20.*
import android.opengl.Matrix
import android.opengl.Matrix.multiplyMV
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

private const val POSITION_COMPONENT_COUNT = 2
private const val COLOR_COMPONENT_COUNT = 4
private const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        COLOR_COMPONENT_COUNT
private const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Grid(width: Float, height: Float, private val rgba: FloatArray,
           private val xSlices: Int, private val ySlices: Int
) {

    private val totalVertices = (xSlices + 1) * (ySlices + 1)
    private val vertices = FloatArray( TOTAL_COMPONENT_COUNT * totalVertices )
    private val vertexArray: VertexArray
    private val vertexIndices = IntBuffer.allocate(xSlices * ySlices * 6)

    init {

        // Initial positions, considering the step size
        val xStep = width / xSlices
        val yStep = height / ySlices
        val x0 = -width/2f
        val y0 = -height/2f

        var offset = 0

        // Since there is a starting and an ending extra point, both loops have an extra round
        for (i in 0..xSlices) {
            for (j in 0..ySlices) {
                // Coordinates
                vertices[offset++] = x0 + i * xStep
                vertices[offset++] = y0 + j * yStep
                // Color
                vertices[offset++] = rgba[0]
                vertices[offset++] = rgba[1]
                vertices[offset++] = rgba[2]
                vertices[offset++] = rgba[3]
            }
        }
        vertexArray = VertexArray(vertices)

        // Order of all the fans (there are 2 triangles per fan: 2 * 3= 6 vertices per quad)
        val fan = IntArray(xSlices * ySlices * 6)
        var fanCounter = 0

        for (i in 0 until xSlices) {
            offset = i * (ySlices + 1)
            for (j in 0 until ySlices) {
                val a = offset
                val b = offset + 1
                val c = (ySlices + 1) * (i + 1) + j + 1
                val d = (ySlices + 1) * (i + 1) + j
                // First triangle
                fan[fanCounter++] = a
                fan[fanCounter++] = b
                fan[fanCounter++] = c
                // Second triangle
                fan[fanCounter++] = a
                fan[fanCounter++] = c
                fan[fanCounter++] = d
                offset ++
            }
        }

        vertexIndices.put(fan)
        vertexIndices.position(0)

    }

    fun updateColor(touchX: Float, touchY: Float, newRgba: FloatArray) {

        val maxValue = 0.16f

        // Iterate through all vertices to check and update each one
        var offset = 0
        for (i in 0 until totalVertices) {

            // Coordinates
            val x = vertices[offset++]
            val y = vertices[offset++]
            val diff = floatArrayOf(touchX - x, touchY - y)
            val length = sqrt(diff[0]*diff[0] + diff[1]*diff[1])

            // Clamp the color change to 0 to maxValue
            if (length < maxValue) {
                // Compute smooth step and add to the color
                var sx = (length - maxValue) / (-maxValue)
                sx *= sx * (3f - 2f * sx)
                vertices[offset++] += newRgba[0] * sx
                vertices[offset++] += newRgba[1] * sx
                vertices[offset++] += newRgba[2] * sx
                offset ++ // Alpha component stays the same
                vertexArray.updateBuffer(vertices, offset - COLOR_COMPONENT_COUNT, COLOR_COMPONENT_COUNT)
            } else {
                offset += COLOR_COMPONENT_COUNT // Skip the color of the current vertex
            }

        }

    }

    fun resetColors() {
        // Change all vertices to the original colors
        var offset = 0
        for (i in 0 until totalVertices) {
            // Skip coordinates
            offset += POSITION_COMPONENT_COUNT
            // Color
            vertices[offset++] = rgba[0]
            vertices[offset++] = rgba[1]
            vertices[offset++] = rgba[2]
            vertices[offset++] = rgba[3]
            vertexArray.updateBuffer(vertices, offset - COLOR_COMPONENT_COUNT, COLOR_COMPONENT_COUNT)
        }
    }

    fun bindData(program: CanvasShaderProgram) {

        vertexArray.setVertexAttribPointer(
            0, program.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT, program.aColorLocation,
            COLOR_COMPONENT_COUNT, STRIDE
        )

    }

    fun draw() {
        // Uncomment to draw solid, comment to draw wireframe
        glDrawElements(GL_TRIANGLES, xSlices * ySlices * 6, GL_UNSIGNED_INT, vertexIndices)
        // Uncomment to draw wireframe, comment to draw solid
        //glDrawElements(GL_LINES, xSlices * ySlices * 6, GL_UNSIGNED_INT, vertexIndices)
    }

}