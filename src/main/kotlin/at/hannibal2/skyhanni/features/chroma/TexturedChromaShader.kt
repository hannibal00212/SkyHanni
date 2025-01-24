package at.hannibal2.skyhanni.features.chroma

/**
 * This chroma type is used for GUI elements with textures, that includes text,
 * any assets from Minecraft or from the mod, etc...
 *
 * **Usage:**
 *
 * If you want to use chroma for text see [FontRendererHook][at.hannibal2.skyhanni.mixins.hooks.FontRendererHook.setupChromaFont]
 *
 * ```
 *      ChromaShaderManager.begin(ChromaType.TEXTURE)
 *      // draw GUI element here
 *      ChromaShaderManager.end()
 * ```
 */
object TexturedChromaShader : ChromaShader("textured_chroma", "textured_chroma") {
    val INSTANCE: TexturedChromaShader
        get() = this
}
