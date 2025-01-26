package at.hannibal2.skyhanni.config.features.fishing.trophyfishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GoldenFishTimerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Golden Fish Timer.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "Nametag",
        desc = "Show a nametag on the Golden Fish showing how weak it is and when it will despawn."
    )
    @ConfigEditorBoolean
    var nametag: Boolean = true

    @Expose
    @ConfigOption(name = "Highlight when ready", desc = "Highlight the Golden Fish when it is ready to be caught.")
    @ConfigEditorBoolean
    var highlight: Boolean = true

    @Expose
    @ConfigOption(
        name = "Throw Rod Warning",
        desc = "Show a warning when you are close to the time limit of throwing your rod."
    )
    @ConfigEditorBoolean
    var throwRodWarning: Boolean = false

    @Expose
    @ConfigOption(name = "Show Head", desc = "Show the Golden Fish head in the Golden Fish Timer GUI.")
    @ConfigEditorBoolean
    var showHead: Boolean = true

    @Expose
    @ConfigOption(name = "Throw Rod Warning Time", desc = "The time in seconds before the throw rod warning appears.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 60f, minStep = 1f)
    var throwRodWarningTime: Int = 20

    @Expose
    @ConfigLink(owner = GoldenFishTimerConfig::class, field = "enabled")
    var position: Position = Position(50, 80)
}
