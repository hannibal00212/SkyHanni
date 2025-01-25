package at.hannibal2.skyhanni.config.features.chroma

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.HasLegacyId
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class ChromaConfig {
    @Expose
    @ConfigOption(name = "Chroma Preview", desc = "§fPlease star SkyHanni on GitHub!")
    @ConfigEditorInfoText(infoTitle = "Only in SkyBlock")
    var chromaPreview: Boolean = false

    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggle SkyHanni's chroma.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Chroma Size", desc = "Change the size of each color in the chroma.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 100f, minStep = 1f)
    var chromaSize: Float = 30f

    @Expose
    @ConfigOption(name = "Chroma Speed", desc = "Change how fast the chroma animation moves.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 20f, minStep = 0.5f)
    var chromaSpeed: Float = 6f

    @Expose
    @ConfigOption(name = "Chroma Saturation", desc = "Change the saturation of the chroma.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1f, minStep = 0.01f)
    var chromaSaturation: Float = 0.75f

    @Expose
    @ConfigOption(name = "Chroma Direction", desc = "Change the slant and direction of the chroma.")
    @ConfigEditorDropdown
    var chromaDirection: Direction = Direction.FORWARD_RIGHT

    enum class Direction(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        FORWARD_RIGHT("Forward + Right", 0),
        FORWARD_LEFT("Forward + Left", 1),
        BACKWARD_RIGHT("Backward + Right", 2),
        BACKWARD_LEFT("Backward + Left", 3);

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @ConfigOption(name = "Reset to Default", desc = "Reset all chroma settings to the default.")
    @ConfigEditorButton(buttonText = "Reset")
    var resetSettings: Runnable = Runnable { ChromaManager.resetChromaSettings() }

    @Expose
    @ConfigOption(
        name = "Everything Chroma",
        desc = "Render §4§l§oALL §r§7text in chroma. §e(Disables Patcher's Optimized Font Renderer while enabled)"
    )
    @ConfigEditorBoolean
    var allChroma: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ignore Chat",
        desc = "Prevent Everything Chroma from applying to the chat (if you unironically use that feature...)"
    )
    @ConfigEditorBoolean
    var ignoreChat: Boolean = false
}
