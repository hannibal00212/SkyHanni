package at.hannibal2.skyhanni.features.chroma

/**
 * A type of chroma shader generally used for standard GUI elements using Gui.drawRect and
 * other GUI element drawing functions.
 *
 * Explicitly those that do not depend on a texture.
 *
 * **Usage:**
 *
 * ```
 *      ChromaShaderManager.begin(ChromaType.STANDARD)
 *      // draw GUI element here
 *      ChromaShaderManager.end()
 * ```
 */
object StandardChromaShader : ChromaShader("standard_chroma", "standard_chroma") {
    val INSTANCE: StandardChromaShader
        get() = this
}
