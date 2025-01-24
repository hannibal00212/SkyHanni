package at.hannibal2.skyhanni.config.features.garden.pests

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PesthunterShopConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Enable the Pesthunter Shop Profit display.")
    @ConfigEditorBoolean
    @FeatureToggle
    var pesthunterProfitEnabled: Boolean = false

    @Expose
    @ConfigOption(name = "Highlight Best Profit", desc = "Highlight the best profit/pest trade.")
    @ConfigEditorBoolean
    var highlightBestProfit: Boolean = true

    @Expose
    @ConfigLink(owner = PesthunterShopConfig::class, field = "pesthunterProfitEnabled")
    var pesthunterProfitPos: Position = Position(206, 158, false, true)
}
