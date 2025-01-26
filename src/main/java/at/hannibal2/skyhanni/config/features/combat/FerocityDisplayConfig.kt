package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FerocityDisplayConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show ferocity stat as single GUI element.\n" +
            "§eRequires tab list widget enabled and ferocity selected to update live."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigLink(owner = FerocityDisplayConfig::class, field = "enabled")
    var position: Position = Position(10, 80, false, true)
}
