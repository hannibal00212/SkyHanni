package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.features.combat.broodmother.BroodmotherConfig
import at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig
import at.hannibal2.skyhanni.config.features.combat.end.EndIslandConfig
import at.hannibal2.skyhanni.config.features.combat.ghostcounter.GhostProfitTrackerConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CombatConfig {
    @Expose
    @Category(name = "Damage Indicator", desc = "Damage Indicator settings")
    var damageIndicator: DamageIndicatorConfig = DamageIndicatorConfig()

    @Expose
    @Category(name = "Ghost Counter", desc = "Ghost Counter settings")
    var ghostCounter: GhostProfitTrackerConfig = GhostProfitTrackerConfig()

    @Expose
    @Category(name = "End Island", desc = "Features for the End Island")
    var endIsland: EndIslandConfig = EndIslandConfig()

    @Expose
    @ConfigOption(name = "Quiver", desc = "")
    @Accordion
    var quiverConfig: QuiverConfig = QuiverConfig()

    // TODO rename to armor stack display
    @Expose
    @ConfigOption(name = "Armor Stack Display", desc = "")
    @Accordion
    var stackDisplayConfig: StackDisplayConfig = StackDisplayConfig()

    @Expose
    @ConfigOption(name = "Summonings", desc = "")
    @Accordion
    var summonings: SummoningsConfig = SummoningsConfig()

    @Expose
    @ConfigOption(name = "Mobs", desc = "")
    @Accordion
    var mobs: MobsConfig = MobsConfig()

    @Expose
    @ConfigOption(name = "Bestiary", desc = "")
    @Accordion
    var bestiary: BestiaryConfig = BestiaryConfig()

    // TODO move into end island config
    @Expose
    @ConfigOption(name = "Ender Node Tracker", desc = "")
    @Accordion
    var enderNodeTracker: EnderNodeConfig = EnderNodeConfig()

    @Expose
    @ConfigOption(name = "Ferocity Display", desc = "")
    @Accordion
    var ferocityDisplay: FerocityDisplayConfig = FerocityDisplayConfig()

    @Expose
    @ConfigOption(name = "Flare", desc = "")
    @Accordion
    var flare: FlareConfig = FlareConfig()

    @Expose
    @ConfigOption(name = "Broodmother", desc = "")
    @Accordion
    var broodmother: BroodmotherConfig = BroodmotherConfig()

    @Expose
    @ConfigOption(name = "Hide Damage Splash", desc = "Hide all damage splashes anywhere in SkyBlock.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideDamageSplash: Boolean = false
}
