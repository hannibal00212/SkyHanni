package at.hannibal2.skyhanni.features.chroma

import at.hannibal2.skyhanni.utils.shader.ShaderManager

/**
 * Object to handle enabling / disabling the chroma shader when rendering text
 *
 * Modified from SkyblockAddons
 *
 * Credit: [MulticolorShaderManager.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/core/chroma/MulticolorShaderManager.java)
 */
object ChromaShaderManager {

    private var chromaEnabled = false

    /**
     * Enables the type of chroma shader passed in
     *
     * @param chromaType A type of chroma shader from [ChromaType]
     */
    fun begin(chromaType: ChromaType) {
        disable()
        enable(chromaType)
    }

    /**
     * Disables the currently active chroma shader
     */
    fun end() {
        disable()
    }

    private fun enable(chromaType: ChromaType) {
        if (!chromaEnabled) {
            chromaEnabled = true
            ShaderManager.enableShader(chromaType.shader)
        }
    }

    private fun disable() {
        if (chromaEnabled) {
            chromaEnabled = false
            ShaderManager.disableShader()
        }
    }
}

enum class ChromaType(val shader: ShaderManager.Shaders) {
    /**
     * See [StandardChromaShader]
     */
    STANDARD(ShaderManager.Shaders.STANDARD_CHROMA),

    /**
     * See [TexturedChromaShader]
     */
    TEXTURED(ShaderManager.Shaders.TEXTURED_CHROMA)
}
