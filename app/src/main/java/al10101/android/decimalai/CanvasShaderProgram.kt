package al10101.android.decimalai

import al10101.android.decimalai.utils.A_COLOR
import al10101.android.decimalai.utils.A_POSITION
import al10101.android.decimalai.utils.ShaderUtils
import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glUseProgram

class CanvasShaderProgram(
    context: Context,
    vertexShaderResId: Int,
    fragmentShaderResId: Int
) {

    private val program by lazy {
        ShaderUtils.buildProgram(
            context.readTextFileFromResource(vertexShaderResId),
            context.readTextFileFromResource(fragmentShaderResId)
        )
    }

    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }

    val aColorLocation by lazy {
        glGetAttribLocation(program, A_COLOR)
    }

    fun useProgram() {
        glUseProgram(program)
    }

}