package at.hannibal2.skyhanni.config.features.slayer.vampire

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BloodIchorConfig {
    @Expose
    @ConfigOption(name = "Highlight Blood Ichor", desc = "Highlight the Blood Ichor.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlight: Boolean = false

    @Expose
    @ConfigOption(name = "Beacon Beam", desc = "Render a beacon beam where the Blood Ichor is.")
    @ConfigEditorBoolean
    @FeatureToggle
    var renderBeam: Boolean = true

    @Expose
    @ConfigOption(name = "Color", desc = "Highlight color.")
    @ConfigEditorColour
    var color: String = "0:199:100:0:88"

    @Expose
    @ConfigOption(
        name = "Show Lines",
        desc = "Draw lines that start from the head of the boss and end on the Blood Ichor."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showLines: Boolean = false

    @Expose
    @ConfigOption(name = "Lines Start Color", desc = "Starting color of the lines.")
    @ConfigEditorColour
    var linesColor: String = "0:255:255:13:0"
}
