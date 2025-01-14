package at.hannibal2.skyhanni.config.features.event.hoppity

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HoppityLocationConfig {
    @Expose
    @ConfigOption(
        name = "Mark Duplicate Locations",
        desc = "Marks egg location waypoints which you have already found in red."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightDuplicates: Boolean = false

    @Expose
    @ConfigOption(name = "Mark Nearby Duplicates", desc = "Always show duplicate egg locations when nearby.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showNearbyDuplicates: Boolean = false

    @Expose
    @ConfigOption(
        name = "Load from NEU PV",
        desc = "Load Hoppity Egg Location data from API when opening the NEU Profile Viewer."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var loadFromNeuPv: Boolean = true
}
