package al10101.android.decimalai

import al10101.android.decimalai.utils.BYTES_PER_FLOAT
import al10101.android.decimalai.utils.VertexArray
import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.IntBuffer

private const val POSITION_COMPONENT_COUNT = 2
private const val COLOR_COMPONENT_COUNT = 4
private const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        COLOR_COMPONENT_COUNT
private const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Grid(width: Float, height: Float, rgba: FloatArray,
           private val xSlices: Int, private val ySlices: Int
) {

    private val vertexArray: VertexArray
    private val vertexIndices = IntBuffer.allocate(xSlices * ySlices * 6)

    init {

        // Initial positions, considering the step size
        val xStep = width / xSlices
        val yStep = height / ySlices
        val x0 = -width/2f
        val y0 = -height/2f

        val vertices = FloatArray( TOTAL_COMPONENT_COUNT * (xSlices+1) * (ySlices+1) )
        var offset = 0

        // Since there is a starting and an ending point extra, both loops have an extra round
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

        // Set data
        vertexArray = VertexArray(vertices)

        // Order of all the fans (there are 4 triangles per fan: 4 * 3= 12 vertices per quad)
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
        glDrawElements(GL_TRIANGLES, xSlices * ySlices * 6, GL_UNSIGNED_INT, vertexIndices)
    }

}