package at.hannibal2.skyhanni.config.features.slayer.vampire

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class KillerSpringConfig {
    @Expose
    @ConfigOption(name = "Highlight Killer Spring", desc = "Highlight the Killer Spring tower.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlight: Boolean = false

    @Expose
    @ConfigOption(name = "Color", desc = "Highlight color.")
    @ConfigEditorColour
    var color: String = "0:199:100:0:88"

    @Expose
    @ConfigOption(
        name = "Show Lines",
        desc = "Draw lines that start from the head of the boss and end on the Killer Spring tower."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showLines: Boolean = false

    @Expose
    @ConfigOption(name = "Lines Start Color", desc = "Starting color of the lines.")
    @ConfigEditorColour
    var linesColor: String = "0:255:255:13:0"
}
