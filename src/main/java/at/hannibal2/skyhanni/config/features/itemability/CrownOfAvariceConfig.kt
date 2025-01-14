package at.hannibal2.skyhanni.config.features.itemability

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
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
        desc = "Have the crown of avarice counter as short format instead of every digit."
    )
    @ConfigEditorBoolean
    var shortFormat: Boolean = true

    @Expose
    @ConfigLink(owner = CrownOfAvariceConfig::class, field = "enable")
    var position: Position = Position(20, 20)
}
