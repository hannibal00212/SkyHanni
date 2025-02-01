package at.hannibal2.skyhanni.config.features.fishing.trophyfishing

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GeyserFishingConfig {
    @Expose
    @ConfigOption(
        name = "Hide Geyser Particles",
        desc = "Stop the white geyser smoke particles from rendering if your bobber is near the geyser."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideParticles: Boolean = true

    @Expose
    @ConfigOption(name = "Draw Geyser Box", desc = "Draw a box around the effective area of the geyser.")
    @ConfigEditorBoolean
    @FeatureToggle
    var drawBox: Boolean = true

    @Expose
    @ConfigOption(name = "Geyser Box Color", desc = "Color of the Geyser Box.")
    @ConfigEditorColour
    var boxColor: String = "0:245:85:255:85"

    @Expose
    @ConfigOption(name = "Only With Rod", desc = "Only render the geyser box while holding a lava rod.")
    @ConfigEditorBoolean
    var onlyWithRod: Boolean = true
}
