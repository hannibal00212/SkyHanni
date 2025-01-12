package at.hannibal2.skyhanni.config.features.slayer.endermen

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class EndermanConfig {
    @Expose
    @ConfigOption(name = "Yang Glyph (Beacon)", desc = "")
    @Accordion
    var beacon: EndermanBeaconConfig = EndermanBeaconConfig()

    @Expose
    @ConfigOption(name = "Highlight Nukekubi Skulls", desc = "Highlight the Enderman Slayer Nukekubi Skulls (Eyes).")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightNukekebi: Boolean = false

    @Expose
    @ConfigOption(name = "Line to Nukekubi Skulls", desc = "Draw a line to the Enderman Slayer Nukekubi Skulls.")
    @ConfigEditorBoolean
    @FeatureToggle
    var drawLineToNukekebi: Boolean = false

    @Expose
    @ConfigOption(name = "Phase Display", desc = "Show the current phase of the Enderman Slayer in damage indicator.")
    @ConfigEditorBoolean
    var phaseDisplay: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide particles around Enderman Slayer bosses and Mini-Bosses.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideParticles: Boolean = false
}
