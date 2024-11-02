package at.hannibal2.skyhanni.utils.shader

import at.hannibal2.skyhanni.utils.LorenzUtils
import org.lwjgl.opengl.ARBFragmentShader
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.ARBVertexShader
import org.lwjgl.opengl.ContextCapabilities
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GLContext

/**
 * Class to check shaders support, OpenGL capabilities, and shader helper functions
 *
 *  Modified class from SkyblockAddons
 *
 *  Credit: [ShaderHelper.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/shader/ShaderHelper.java)
 */
object ShaderHelper {

    private var SHADERS_SUPPORTED: Boolean

    private var USING_ARB_SHADERS: Boolean

    var GL_LINK_STATUS: Int
    var GL_COMPILE_STATUS: Int
    var GL_VERTEX_SHADER: Int
    var GL_FRAGMENT_SHADER: Int

    init {
        val capabilities: ContextCapabilities = GLContext.getCapabilities()

        // Check OpenGL 2.0 Capabilities
        val openGL20supported = capabilities.OpenGL20
        SHADERS_SUPPORTED = openGL20supported ||
            capabilities.GL_ARB_vertex_shader &&
            capabilities.GL_ARB_fragment_shader &&
            capabilities.GL_ARB_shader_objects

        var log = "Shaders are"
        if (!SHADERS_SUPPORTED) log += " not"
        log += " available. "

        if (SHADERS_SUPPORTED) {
            if (capabilities.OpenGL20) {
                log += "OpenGL 2.0 is supported. "
                USING_ARB_SHADERS = false
                GL_LINK_STATUS = GL20.GL_LINK_STATUS
                GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS
                GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER
                GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER
            } else {
                log += "ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported. "
                USING_ARB_SHADERS = true
                GL_LINK_STATUS = ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB
                GL_COMPILE_STATUS = ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB
                GL_VERTEX_SHADER = ARBVertexShader.GL_VERTEX_SHADER_ARB
                GL_FRAGMENT_SHADER = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB
            }
        } else {
            log += "OpenGL 2.0 is not supported and ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are not supported."
            USING_ARB_SHADERS = false
            GL_LINK_STATUS = GL11.GL_FALSE
            GL_COMPILE_STATUS = GL11.GL_FALSE
            GL_VERTEX_SHADER = GL11.GL_FALSE
            GL_FRAGMENT_SHADER = GL11.GL_FALSE
        }

        LorenzUtils.consoleLog(log)
    }

    fun glLinkProgram(program: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glLinkProgramARB(program) else GL20.glLinkProgram(program)
    }

    fun glGetProgramInfoLog(program: Int, maxLength: Int): String {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetInfoLogARB(
            program,
            maxLength
        ) else GL20.glGetProgramInfoLog(program, maxLength)
    }

    fun glGetProgramInt(program: Int, pName: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetObjectParameteriARB(
            program,
            pName
        ) else GL20.glGetProgrami(program, pName)
    }

    fun glUseProgram(program: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glUseProgramObjectARB(program) else GL20.glUseProgram(program)
    }

    fun glAttachShader(program: Int, shaderIn: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glAttachObjectARB(program, shaderIn) else GL20.glAttachShader(
            program,
            shaderIn
        )
    }

    fun glCreateShader(type: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glCreateShaderObjectARB(type) else GL20.glCreateShader(type)
    }

    fun glShaderSource(shader: Int, source: CharSequence) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glShaderSourceARB(shader, source) else GL20.glShaderSource(
            shader,
            source
        )
    }

    fun glCompileShader(shader: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glCompileShaderARB(shader) else GL20.glCompileShader(shader)
    }

    fun glGetShaderInt(shader: Int, pName: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetObjectParameteriARB(
            shader,
            pName
        ) else GL20.glGetShaderi(shader, pName)
    }

    fun glGetShaderInfoLog(shader: Int, maxLength: Int): String {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetInfoLogARB(
            shader,
            maxLength
        ) else GL20.glGetShaderInfoLog(shader, maxLength)
    }

    fun glDeleteShader(shader: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glDeleteObjectARB(shader) else GL20.glDeleteShader(shader)
    }

    fun glUniform1f(location: Int, v0: Float) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glUniform1fARB(location, v0) else GL20.glUniform1f(location, v0)
    }

    fun glUniform1i(location: Int, v0: Int) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glUniform1iARB(location, v0) else GL20.glUniform1i(location, v0)
    }

    fun glUniform2f(location: Int, v0: Float, v1: Float) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glUniform2fARB(location, v0, v1) else GL20.glUniform2f(
            location,
            v0,
            v1
        )
    }

    fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        if (USING_ARB_SHADERS) ARBShaderObjects.glUniform3fARB(location, v0, v1, v2) else GL20.glUniform3f(
            location,
            v0,
            v1,
            v2
        )
    }

    fun glGetUniformLocation(program: Int, name: CharSequence): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetUniformLocationARB(
            program,
            name
        ) else GL20.glGetUniformLocation(program, name)
    }

    fun areShadersSupported() = SHADERS_SUPPORTED
}
