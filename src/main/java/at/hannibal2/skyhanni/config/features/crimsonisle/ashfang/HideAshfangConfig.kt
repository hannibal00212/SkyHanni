package at.hannibal2.skyhanni.config.features.crimsonisle.ashfang

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HideAshfangConfig {
    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide particles around the Ashfang boss.")
    @ConfigEditorBoolean
    @FeatureToggle
    var particles: Boolean = false

    @Expose
    @ConfigOption(
        name = "Hide Full Names",
        desc = "Hide the names of full health blazes around Ashfang §e(only useful when highlight blazes is enabled)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var fullNames: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Damage Splash", desc = "Hide damage splashes around Ashfang.")
    @ConfigEditorBoolean
    @FeatureToggle
    var damageSplash: Boolean = false
}
