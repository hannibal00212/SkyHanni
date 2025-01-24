package at.hannibal2.skyhanni.utils.shader

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.client.shader.ShaderLinkHelper
import org.apache.commons.lang3.StringUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.OpenGLException

/**
 * Superclass for shader objects to compile and attach vertex and fragment shaders to the shader program
 *
 * Modified class from SkyblockAddons
 *
 * Credit: [Shader.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/shader/Shader.java)
 */
abstract class Shader(val vertex: String, val fragment: String) {

    var shaderProgram: Int = -1
    private var vertexShaderID: Int = -1
    private var fragmentShaderID: Int = -1

    private val uniforms: MutableList<Uniform<*>> = mutableListOf()

    var created = false

    init {
        recompile()
        (Minecraft.getMinecraft().resourceManager as IReloadableResourceManager).registerReloadListener {
            recompile()
        }
    }

    fun deleteOldShaders() {
        if (vertexShaderID >= 0) {
            OpenGlHelper.glDeleteShader(vertexShaderID)
            vertexShaderID = -1
        }
        if (fragmentShaderID >= 0) {
            OpenGlHelper.glDeleteShader(fragmentShaderID)
            fragmentShaderID = -1
        }
        if (shaderProgram >= 0) {
            OpenGlHelper.glDeleteProgram(shaderProgram)
            shaderProgram = -1
        }
        uniforms.clear()
        created = false
    }

    fun recompile() {
        deleteOldShaders()
        shaderProgram = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram()
        if (shaderProgram < 0) return

        vertexShaderID = ShaderManager.loadShader(ShaderType.VERTEX, vertex)
        if (vertexShaderID < 0) return
        ShaderManager.attachShader(shaderProgram, vertexShaderID)

        fragmentShaderID = ShaderManager.loadShader(ShaderType.FRAGMENT, fragment)
        if (fragmentShaderID < 0) return
        ShaderManager.attachShader(shaderProgram, fragmentShaderID)

        ShaderHelper.glLinkProgram(shaderProgram)

        if (ShaderHelper.glGetProgramInt(shaderProgram, ShaderHelper.GL_LINK_STATUS) == GL11.GL_FALSE) {
            val errorMessage = "Failed to link vertex shader $vertex and fragment shader $fragment. Features that " +
                "utilise this shader will not work correctly, if at all"
            val errorLog = StringUtils.trim(ShaderHelper.glGetShaderInfoLog(shaderProgram, 1024))

            if (ShaderManager.inWorld()) {
                ErrorManager.logErrorWithData(
                    OpenGLException("Shader linking error."),
                    errorMessage,
                    "Link Error:\n" to errorLog
                )
            } else {
                LorenzUtils.consoleLog("$errorMessage $errorLog")
            }
            return
        }

        this.registerUniforms()
        created = true
    }

    abstract fun registerUniforms()

    fun updateUniforms() {
        for (uniform in uniforms) {
            uniform.update()
        }
    }

    fun enable() = ShaderHelper.glUseProgram(shaderProgram)

    fun disable() = ShaderHelper.glUseProgram(0)

    /**
     * @param uniformType Type of uniform, there should be a 1 to 1 equivalent to that in the shader file
     * @param name The name of the uniform in the shader file. This should match exactly to the name given
     * to the uniform in the shader file.
     * @param uniformValuesSupplier The supplier that changes / sets the uniform's value
     */
    fun <T> registerUniform(uniformType: Uniform.UniformType<T>, name: String, uniformValuesSupplier: () -> T) {
        uniforms.add(Uniform(this, uniformType, name, uniformValuesSupplier))
    }
}
