package al10101.android.decimalai

import al10101.android.decimalai.utils.ShaderUtils
import android.content.Context
import android.opengl.GLES20.glUseProgram

const val U_PROJECTION_MATRIX = "u_ProjectionMatrix"
const val U_TEXTURE_UNIT = "u_TextureUnit"

const val A_POSITION = "a_Position"
const val A_COLOR = "a_Color"
const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

open class ShaderProgram(
    context: Context,
    vertexShaderResId: Int,
    fragmentShaderResId: Int
) {

    val program by lazy {
        ShaderUtils.buildProgram(
            context.readTextFileFromResource(vertexShaderResId),
            context.readTextFileFromResource(fragmentShaderResId)
        )
    }

    fun useProgram() {
        glUseProgram(program)
    }

}