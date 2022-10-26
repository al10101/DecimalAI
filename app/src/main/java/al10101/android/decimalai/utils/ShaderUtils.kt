package al10101.android.decimalai.utils

import android.opengl.GLES20.*
import android.util.Log

private const val OPENGL_ZERO = 0

object ShaderUtils {

    private fun loadShader(shaderType: Int, source: String): Int {

        val shader = glCreateShader(shaderType)
        if (shader != OPENGL_ZERO) {

            // Create and compile shader
            glShaderSource(shader, source)
            glCompileShader(shader)

            // Check for any errors
            val compiled = IntArray(1)
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)

            if (compiled[0] == OPENGL_ZERO) {
                Log.e(SHADER_TAG, "Could not compile shader $shaderType:")
                Log.e(SHADER_TAG, glGetShaderInfoLog(shader))
                glDeleteShader(shader)
                return OPENGL_ZERO
            }

        }

        return shader

    }

    private fun validateProgram(program: Int) {

        // Do the actual validation
        glValidateProgram(program)

        // Check the status
        val validateStatus = IntArray(1)
        glGetProgramiv(program, GL_VALIDATE_STATUS, validateStatus, 0)

        val success = validateStatus[0] != OPENGL_ZERO
        if (!success) {
            Log.v(SHADER_TAG, "Results of the validating program nr. $program: $success")
            Log.v(SHADER_TAG, glGetProgramInfoLog(program))
        }

    }

    fun buildProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {

        // Load both programs and return their respective handles
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Check if any errors occurred
        if (vertexShader == OPENGL_ZERO || fragmentShader == OPENGL_ZERO) {
            return OPENGL_ZERO
        }

        // Creates an empty program object to host the two shaders
        val program = glCreateProgram()

        if (program != OPENGL_ZERO) {

            // Attach both shaders to the program
            glAttachShader(program, vertexShader)
            glAttachShader(program, fragmentShader)

            // Link the program to OpenGL pipeline
            glLinkProgram(program)

            // Check for any possible errors
            val linkStatus = IntArray(1)
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                Log.e(SHADER_TAG, "Could not link program:")
                Log.e(SHADER_TAG, glGetProgramInfoLog(program))
                glDeleteProgram(program)
                return OPENGL_ZERO
            }

        }

        validateProgram(program)

        return program

    }

}