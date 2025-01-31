package at.hannibal2.skyhanni.config.features.crimsonisle.ashfang

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BlazingSoulsColor {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the Blazing Souls more clearly.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Souls Color", desc = "Color of the Blazing Souls.")
    @ConfigEditorColour
    var color: String = "0:245:85:255:85"
}
