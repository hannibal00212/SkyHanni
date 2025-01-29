package at.hannibal2.skyhanni.config.features.stranded

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class StrandedConfig {
    @Expose
    @ConfigOption(
        name = "Highlight Placeable NPCs",
        desc = "Highlight NPCs that can be placed, but aren't, in the NPC menu."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightPlaceableNpcs: Boolean = false

    @Expose
    @ConfigOption(name = "In Water Display", desc = "Display if the player is in water.")
    @ConfigEditorBoolean
    @FeatureToggle
    var inWaterDisplay: Boolean = false

    @Expose
    @ConfigLink(owner = StrandedConfig::class, field = "inWaterDisplay")
    var inWaterPosition: Position = Position(20, 20)
}
