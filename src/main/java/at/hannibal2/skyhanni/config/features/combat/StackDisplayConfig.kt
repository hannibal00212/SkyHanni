package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class StackDisplayConfig {
    @Expose
    @ConfigOption(
        name = "Enable",
        desc = "Display the number of stacks on armor pieces like Crimson, Terror, Aurora etc."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigLink(owner = StackDisplayConfig::class, field = "enabled")
    var position: Position = Position(480, -210, 1.9f)
}
