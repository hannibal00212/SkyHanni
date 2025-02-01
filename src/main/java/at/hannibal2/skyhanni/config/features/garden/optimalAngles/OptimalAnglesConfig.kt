package at.hannibal2.skyhanni.config.features.garden.optimalAngles

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class OptimalAnglesConfig {
    @Expose
    @ConfigOption(
        name = "Squeaky Mousemat",
        desc = "Set the optimal angles (pitch & yaw) in the Squeaky Mousemat overlay by clicking on the presets."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var signEnabled: Boolean = true

    @Expose
    @ConfigOption(name = "Compact GUI", desc = "Compact the Squeaky Mousemat GUI only showing crop icons.")
    @ConfigEditorBoolean
    var compactMousematGui: Boolean = false

    @Expose
    @ConfigLink(owner = OptimalAnglesConfig::class, field = "signEnabled")
    var signPosition: Position = Position(20, -195, false, true)

    @Expose
    @ConfigOption(name = "Custom Speed", desc = "Change the exact speed for every single crop.")
    @Accordion
    var customAngles: CustomAnglesConfig = CustomAnglesConfig()
}
