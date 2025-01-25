package at.hannibal2.skyhanni.features.chroma

import at.hannibal2.skyhanni.config.features.chroma.ChromaConfig.Direction
import at.hannibal2.skyhanni.data.MinecraftData
import at.hannibal2.skyhanni.mixins.transformers.AccessorMinecraft
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.shader.Shader
import at.hannibal2.skyhanni.utils.shader.Uniform
import net.minecraft.client.Minecraft

/**
 * Modified from SkyblockAddons
 *
 * Credit: [ChromaShader.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/shader/chroma/ChromaShader.java)
 */

abstract class ChromaShader(vertex: String, fragment: String) : Shader(vertex, fragment) {

    override fun registerUniforms() {
        registerUniform(Uniform.UniformType.FLOAT, "chromaSize") {
            ChromaManager.config.chromaSize * (GuiScreenUtils.displayWidth / 100f)
        }
        registerUniform(Uniform.UniformType.FLOAT, "timeOffset") {
            var ticks =
                (MinecraftData.totalTicks) + (Minecraft.getMinecraft() as AccessorMinecraft).timer.renderPartialTicks

            ticks = when (ChromaManager.config.chromaDirection) {
                Direction.FORWARD_RIGHT, Direction.BACKWARD_RIGHT -> ticks
                Direction.FORWARD_LEFT, Direction.BACKWARD_LEFT -> -ticks
                else -> ticks
            }

            val chromaSpeed = ChromaManager.config.chromaSpeed / 360f
            ticks * chromaSpeed
        }
        registerUniform(Uniform.UniformType.FLOAT, "saturation") {
            ChromaManager.config.chromaSaturation
        }
        registerUniform(Uniform.UniformType.BOOL, "forwardDirection") {
            when (ChromaManager.config.chromaDirection) {
                Direction.FORWARD_RIGHT, Direction.FORWARD_LEFT -> true
                Direction.BACKWARD_RIGHT, Direction.BACKWARD_LEFT -> false
                else -> true
            }
        }
    }
}
