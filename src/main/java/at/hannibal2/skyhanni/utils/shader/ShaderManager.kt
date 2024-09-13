package at.hannibal2.skyhanni.utils.shader

import at.hannibal2.skyhanni.features.chroma.StandardChromaShader
import at.hannibal2.skyhanni.features.chroma.TexturedChromaShader
import at.hannibal2.skyhanni.features.misc.DarkenShader
import at.hannibal2.skyhanni.features.misc.RoundedRectangleOutlineShader
import at.hannibal2.skyhanni.features.misc.RoundedRectangleShader
import at.hannibal2.skyhanni.features.misc.RoundedTextureShader
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.apache.commons.lang3.StringUtils
import org.lwjgl.opengl.OpenGLException
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Object to handle shaders for SkyHanni
 */
object ShaderManager {

    /**
     * For any future shaders add the object instance in this enum and
     * in the when-expression
     */
    enum class Shaders(val shader: Shader) {
        STANDARD_CHROMA(StandardChromaShader.INSTANCE),
        TEXTURED_CHROMA(TexturedChromaShader.INSTANCE),
        ROUNDED_RECTANGLE(RoundedRectangleShader.INSTANCE),
        ROUNDED_RECT_OUTLINE(RoundedRectangleOutlineShader.INSTANCE),
        ROUNDED_TEXTURE(RoundedTextureShader.INSTANCE),
        DARKEN(DarkenShader.INSTANCE)
        ;

        fun enableShader() = enableShader(this)
    }

    private var activeShader: Shader? = null

    fun enableShader(shader: Shaders) {
        val shaderInstance = shader.shader

        if (!shaderInstance.created) return

        activeShader = shaderInstance
        shaderInstance.enable()
        shaderInstance.updateUniforms()
    }

    fun attachShader(shaderProgram: Int, shaderID: Int) {
        ShaderHelper.glAttachShader(shaderProgram, shaderID)
    }

    fun disableShader() {
        if (activeShader == null) return

        activeShader?.disable()
        activeShader = null
    }

    fun loadShader(type: ShaderType, fileName: String): Int {
        val resourceLocation = ResourceLocation("skyhanni:shaders/$fileName${type.extension}")

        val source = StringBuilder()

        val inputStream = Minecraft.getMinecraft().resourceManager.getResource(resourceLocation).inputStream
        BufferedReader(InputStreamReader(inputStream)).forEachLine {
            source.append(it).append("\n")
        }

        val shaderID = ShaderHelper.glCreateShader(type.shaderType)
        ShaderHelper.glShaderSource(shaderID, source.toString())
        ShaderHelper.glCompileShader(shaderID)

        if (ShaderHelper.glGetShaderInt(shaderID, ShaderHelper.GL_COMPILE_STATUS) == 0) {
            val errorMessage = "Failed to compile shader $fileName${type.extension}. Features that utilise this " +
                "shader will not work correctly, if at all"
            val errorLog = StringUtils.trim(ShaderHelper.glGetShaderInfoLog(shaderID, 1024))

            if (inWorld()) {
                ErrorManager.logErrorWithData(
                    OpenGLException("Shader compilation error."),
                    errorMessage,
                    "GLSL Compilation Error:\n" to errorLog,
                )
            } else {
                LorenzUtils.consoleLog("$errorMessage $errorLog")
            }

            return -1
        }

        return shaderID
    }

    fun inWorld() = (Minecraft.getMinecraft().theWorld != null) && (Minecraft.getMinecraft().thePlayer != null)
}

enum class ShaderType(val extension: String, val shaderType: Int) {
    VERTEX(".vsh", ShaderHelper.GL_VERTEX_SHADER),
    FRAGMENT(".fsh", ShaderHelper.GL_FRAGMENT_SHADER)
}
