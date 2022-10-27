package al10101.android.decimalai

import android.content.Context
import android.opengl.GLES20.*

class CanvasShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.canvas_vertex_shader,
    R.raw.canvas_fragment_shader
) {

    private val uProjectionMatrixLocation by lazy {
        glGetUniformLocation(program, U_PROJECTION_MATRIX)
    }

    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }

    val aColorLocation by lazy {
        glGetAttribLocation(program, A_COLOR)
    }

    fun setUniforms(projectionMatrix: FloatArray) {
        glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, projectionMatrix, 0)
    }

}