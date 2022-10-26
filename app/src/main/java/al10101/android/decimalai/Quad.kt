package al10101.android.decimalai

import al10101.android.decimalai.utils.BYTES_PER_FLOAT
import al10101.android.decimalai.utils.VertexArray
import android.opengl.GLES20.*
import java.nio.ByteBuffer

private const val POSITION_COMPONENT_COUNT = 2
private const val COLOR_COMPONENT_COUNT = 4
private const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        COLOR_COMPONENT_COUNT
private const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Quad(width: Float, height: Float, rgba: FloatArray) {

    private val vertexArray: VertexArray
    private val fanOrder = ByteBuffer.allocateDirect(6)

    init {

        // Since it will be drawn with the fan mode, we add positions and fan

        val positions = floatArrayOf( // Order of coordinates: XY RGBA
            -width/2f, -height/2f, rgba[0], rgba[1], rgba[2], rgba[3],
            -width/2f,  height/2f, rgba[0], rgba[1], rgba[2], rgba[3],
             width/2f,  height/2f, rgba[0], rgba[1], rgba[2], rgba[3],
             width/2f, -height/2f, rgba[0], rgba[1], rgba[2], rgba[3],
        )
        vertexArray = VertexArray(positions)

        val fan = byteArrayOf(0, 1, 2, 0, 2, 3)
        fanOrder.put(fan)
        fanOrder.position(0)

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
        glDrawElements(GL_TRIANGLE_FAN, 6, GL_UNSIGNED_BYTE, fanOrder)
    }

}