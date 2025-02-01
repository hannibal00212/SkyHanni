package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ThunderSparkConfig {
    @Expose
    @ConfigOption(name = "Thunder Spark Highlight", desc = "Highlight Thunder Sparks after killing a Thunder.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlight: Boolean = false

    @Expose
    @ConfigOption(name = "Thunder Spark Color", desc = "Color of the Thunder Sparks.")
    @ConfigEditorColour
    var color: String = "0:255:255:255:255"
}
