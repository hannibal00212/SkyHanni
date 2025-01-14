package at.hannibal2.skyhanni.config.features.minion

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class EmptiedTimeConfig {
    @Expose
    @ConfigOption(name = "Emptied Time Display", desc = "Show the time when the hopper in the minion was last emptied.")
    @ConfigEditorBoolean
    @FeatureToggle
    var display: Boolean = false

    @Expose
    @ConfigOption(name = "Distance", desc = "Maximum distance to display minion data.")
    @ConfigEditorSlider(minValue = 3f, maxValue = 30f, minStep = 1f)
    var distance: Int = 10
}
