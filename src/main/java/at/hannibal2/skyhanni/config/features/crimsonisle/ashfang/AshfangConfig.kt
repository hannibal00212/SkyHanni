package at.hannibal2.skyhanni.config.features.crimsonisle.ashfang

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class AshfangConfig {
    @ConfigOption(name = "Gravity Orbs", desc = "")
    @Accordion
    @Expose
    var gravityOrbs: GravityOrbsConfig = GravityOrbsConfig()

    @ConfigOption(name = "Blazing Souls", desc = "")
    @Accordion
    @Expose
    var blazingSouls: BlazingSoulsColor = BlazingSoulsColor()

    @ConfigOption(name = "Hide Stuff", desc = "")
    @Accordion
    @Expose
    var hide: HideAshfangConfig = HideAshfangConfig()

    @Expose
    @ConfigOption(name = "Highlight Blazes", desc = "Highlight the different blazes in their respective colors.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightBlazes: Boolean = false

    @Expose
    @ConfigOption(name = "Freeze Cooldown", desc = "Show the cooldown for how long Ashfang blocks your abilities.")
    @ConfigEditorBoolean
    @FeatureToggle
    var freezeCooldown: Boolean = false

    @Expose
    @ConfigLink(owner = AshfangConfig::class, field = "freezeCooldown")
    var freezeCooldownPos: Position = Position(10, 10, false, true)

    @Expose
    @ConfigOption(name = "Reset Time", desc = "Show the cooldown until Ashfang pulls his underlings back.")
    @ConfigEditorBoolean
    @FeatureToggle
    var nextResetCooldown: Boolean = false

    @Expose
    @ConfigLink(owner = AshfangConfig::class, field = "nextResetCooldown")
    var nextResetCooldownPos: Position = Position(10, 10, false, true)
}
