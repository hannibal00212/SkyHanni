package at.hannibal2.skyhanni.config.features.rift.area.mountaintop

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SunGeckoConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show Sun Gecko Helper.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Show Modifiers", desc = "Show a list of modifiers in the overlay.")
    @ConfigEditorBoolean
    var showModifiers: Boolean = false

    @Expose
    @ConfigOption(name = "Highlight Real Boss", desc = "Highlights the real boss in green.")
    @ConfigEditorBoolean
    var highlightRealBoss: Boolean = false

    @Expose
    @ConfigOption(name = "Highlight Clones", desc = "Highlights the fakes bosses in red.")
    @ConfigEditorBoolean
    var highlightFakeBoss: Boolean = true

    @Expose
    @ConfigLink(owner = SunGeckoConfig::class, field = "enabled")
    var pos: Position = Position(-256, 140, false, true)
}
