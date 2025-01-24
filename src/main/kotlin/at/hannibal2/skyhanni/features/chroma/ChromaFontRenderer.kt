package at.hannibal2.skyhanni.features.chroma

import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.shader.ShaderHelper
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

/**
 * Class to handle chroma font rendering
 *
 * Modified class from SkyblockAddons
 *
 * Credit: [DrawStateFontRenderer.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/utils/draw/DrawStateFontRenderer.java)
 */
class ChromaFontRenderer(private val baseColor: Int) {

    private var chromaOn = false

    fun startChroma() {
        chromaOn = true
    }

    fun endChroma() {
        chromaOn = false
    }

    fun loadChromaEnv() {
        if (chromaOn) {
            newChromaEnv()
        }
    }

    fun restoreChromaEnv() {
        if (ShaderHelper.areShadersSupported() && !chromaOn) ChromaShaderManager.end()
    }

    fun newChromaEnv(): ChromaFontRenderer {
        if (ShaderHelper.areShadersSupported()) {
            ChromaShaderManager.begin(ChromaType.TEXTURED)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        }
        return this
    }

    fun bindActualColor(alpha: Float): ChromaFontRenderer {
        GlStateManager.color(
            ColorUtils.getRed(baseColor).toFloat() / 255f,
            ColorUtils.getGreen(baseColor).toFloat() / 255f,
            ColorUtils.getBlue(baseColor).toFloat() / 255f,
            alpha
        )
        return this
    }

    fun endChromaEnv(): ChromaFontRenderer {
        if (ShaderHelper.areShadersSupported()) {
            ChromaShaderManager.end()
            GlStateManager.shadeModel(GL11.GL_FLAT)
        }
        return this
    }

    fun getChromaState() = chromaOn
}
