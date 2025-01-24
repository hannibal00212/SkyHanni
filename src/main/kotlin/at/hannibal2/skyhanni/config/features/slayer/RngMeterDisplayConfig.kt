package at.hannibal2.skyhanni.config.features.slayer

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RngMeterDisplayConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Display amount of bosses needed until next RNG meter drop.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Warn Empty", desc = "Warn when no item is set in the RNG Meter.")
    @ConfigEditorBoolean
    var warnEmpty: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the RNG meter message from chat if current item is selected.")
    @ConfigEditorBoolean
    var hideChat: Boolean = true

    @Expose
    @ConfigLink(owner = RngMeterDisplayConfig::class, field = "enabled")
    var pos: Position = Position(410, 110, false, true)
}
