package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FishingBaitWarningsConfig {
    @Expose
    @ConfigOption(name = "Bait Change Warning", desc = "Show warning when fishing bait is changed")
    @ConfigEditorBoolean
    @FeatureToggle
    var baitChangeWarning: Boolean = false

    @Expose
    @ConfigOption(name = "No Bait Warning", desc = "Show warning when no bait is used")
    @ConfigEditorBoolean
    @FeatureToggle
    var noBaitWarning: Boolean = false
}
