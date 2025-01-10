package at.hannibal2.skyhanni.config.features.slayer

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SlayerProfitTrackerConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Count all items you pick up while doing slayer, " +
            "keeping track of how much you pay for starting slayers and calculating the overall profit."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigLink(owner = SlayerProfitTrackerConfig::class, field = "enabled")
    var pos: Position = Position(20, 20, false, true)
}
