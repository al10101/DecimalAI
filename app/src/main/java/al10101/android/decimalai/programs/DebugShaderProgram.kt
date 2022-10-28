package al10101.android.decimalai.programs

import al10101.android.decimalai.*
import android.content.Context
import android.opengl.GLES20.*

class DebugShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.debug_vertex_shader,
    R.raw.debug_fragment_shader
) {

    private val uTextureUnitLocation by lazy {
        glGetUniformLocation(program, U_TEXTURE_UNIT)
    }

    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }

    val aTextureCoordinatesLocation by lazy {
        glGetAttribLocation(program, A_TEXTURE_COORDINATES)
    }

    fun setUniforms(textureId: Int) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(uTextureUnitLocation, 0)
    }

}