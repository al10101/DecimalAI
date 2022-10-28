package al10101.android.decimalai.model

import al10101.android.decimalai.programs.DebugShaderProgram
import al10101.android.decimalai.utils.BYTES_PER_FLOAT
import al10101.android.decimalai.utils.VertexArray
import android.opengl.GLES20.*
import java.nio.ByteBuffer

private const val POSITION_COMPONENT_COUNT = 2
private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
private const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        TEXTURE_COORDINATES_COMPONENT_COUNT
private const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Quad(width: Float, height: Float) {

    private val vertexArray: VertexArray
    private val fanOrder = ByteBuffer.allocateDirect(6)

    init {

        // Since it will be drawn with the fan mode, we add position and fan
        val positions = floatArrayOf(
            // Order of coordinates X Y S T
            -width/2f, -height/2f, 0f, 0f,
            -width/2f,  height/2f, 0f, 1f,
             width/2f,  height/2f, 1f, 1f,
             width/2f, -height/2f, 1f, 0f,
        )
        vertexArray = VertexArray(positions)

        val fan = byteArrayOf(0, 1, 2, 0, 2, 3)
        fanOrder.put(fan)
        fanOrder.position(0)

    }

    fun bindData(debugProgram: DebugShaderProgram) {

        vertexArray.setVertexAttribPointer(
            0, debugProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT, debugProgram.aTextureCoordinatesLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE
        )

    }

    fun draw() {
        glDrawElements(GL_TRIANGLE_FAN, 6, GL_UNSIGNED_BYTE, fanOrder)
    }

}