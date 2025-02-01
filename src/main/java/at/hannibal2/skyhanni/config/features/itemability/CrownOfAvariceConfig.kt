package at.hannibal2.skyhanni.config.features.itemability

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CrownOfAvariceConfig {
    @Expose
    @ConfigOption(name = "Counter", desc = "Shows the current coins of your crown of avarice (if worn).")
    @ConfigEditorBoolean
    @FeatureToggle
    var enable: Boolean = false

    @Expose
    @ConfigOption(
        name = "Counter format",
        desc = "Have the crown of avarice counter as short format instead of every digit.",
    )
    @ConfigEditorBoolean
    var shortFormat: Boolean = true

    @Expose
    @ConfigOption(name = "Coins Per Hour format", desc = "Shows the coins per hour gained as short format i.e. 7.3M.")
    @ConfigEditorBoolean
    var shortFormatCPH: Boolean = true

    @Expose
    @ConfigOption(name = "Crown Coins Per Hour", desc = "Show coins per hour in the Avarice Counter.")
    @ConfigEditorBoolean
    var perHour: Boolean = false

    @Expose
    @ConfigOption(name = "Time until Max Crown", desc = "Shows the time until you reach max coins (1B coins).")
    @ConfigEditorBoolean
    var time: Boolean = false

    @Expose
    @ConfigOption(name = "Last coins gained", desc = "Shows the amount of scavenger coins gained by last killed mob.")
    @ConfigEditorBoolean
    var coinDiff: Boolean = false

    @Expose
    @ConfigOption(
        name = "Session Active Timer",
        desc = "Waits the duration (in seconds) before session statistics are displayed after loading in."
    )
    @ConfigEditorSlider(minValue = 0F, maxValue = 10F, minStep = 1F)
    var sessionActiveTime: Int = 10

    @Expose
    @ConfigLink(owner = CrownOfAvariceConfig::class, field = "enable")
    var position: Position = Position(20, 20)
}
