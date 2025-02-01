package at.hannibal2.skyhanni.config.features.crimsonisle.ashfang

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GravityOrbsConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the Gravity Orbs more clearly.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the Gravity Orbs.")
    @ConfigEditorColour
    var color: String = "0:120:255:85:85"
}
