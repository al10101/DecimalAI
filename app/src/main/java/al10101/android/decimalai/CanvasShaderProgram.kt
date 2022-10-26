package al10101.android.decimalai

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation

class CanvasShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.canvas_vertex_shader,
    R.raw.canvas_fragment_shader
) {

    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }

    val aColorLocation by lazy {
        glGetAttribLocation(program, A_COLOR)
    }

}