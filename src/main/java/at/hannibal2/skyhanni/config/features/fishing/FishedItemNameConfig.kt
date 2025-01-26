package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FishedItemNameConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the fished item name above the item when fishing.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Show Bait", desc = "Also show the name of the consumed bait.")
    @ConfigEditorBoolean
    var showBaits: Boolean = false
}
