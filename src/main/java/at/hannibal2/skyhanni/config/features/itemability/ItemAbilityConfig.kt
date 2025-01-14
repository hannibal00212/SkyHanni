package at.hannibal2.skyhanni.config.features.itemability

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ItemAbilityConfig {
    // TODO rename to "enabled"
    @Expose
    @ConfigOption(name = "Ability Cooldown", desc = "Show the cooldown of item abilities.")
    @ConfigEditorBoolean
    @FeatureToggle
    var itemAbilityCooldown: Boolean = false

    // TODO rename to "background"
    @Expose
    @ConfigOption(
        name = "Ability Cooldown Background",
        desc = "Show the cooldown color of item abilities in the background."
    )
    @ConfigEditorBoolean
    var itemAbilityCooldownBackground: Boolean = false

    @Expose
    @ConfigOption(name = "Show When Ready", desc = "Show the R and background (if enabled) when the ability is ready.")
    @ConfigEditorBoolean
    var itemAbilityShowWhenReady: Boolean = true

    @Expose
    @ConfigOption(name = "Fire Veil", desc = "")
    @Accordion
    var fireVeilWands: FireVeilWandConfig = FireVeilWandConfig()

    @ConfigOption(name = "Chicken Head", desc = "")
    @Accordion
    @Expose
    var chickenHead: ChickenHeadConfig = ChickenHeadConfig()

    @ConfigOption(name = "Crown of Avarice", desc = "")
    @Accordion
    @Expose
    var crownOfAvarice: CrownOfAvariceConfig = CrownOfAvariceConfig()

    @Expose
    @ConfigOption(
        name = "Depleted Bonzo's Masks",
        desc = "Highlight used Bonzo's Masks and Spirit Masks with a background."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var depletedBonzosMasks: Boolean = false
}
