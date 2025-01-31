package at.hannibal2.skyhanni.config.features.minion

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LastClickedMinionConfig {
    @Expose
    @ConfigOption(
        name = "Last Minion Display",
        desc = "Mark the location of the last clicked minion, even through walls."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var display: Boolean = false

    @Expose
    @ConfigOption(name = "Last Minion Color", desc = "The color in which the last minion should be displayed.")
    @ConfigEditorColour
    var color: String = "0:245:85:255:85"

    @Expose
    @ConfigOption(name = "Last Minion Time", desc = "Time in seconds how long the last minion should be displayed.")
    @ConfigEditorSlider(minValue = 3f, maxValue = 120f, minStep = 1f)
    var time: Int = 20
}
