package at.hannibal2.skyhanni.config.features.minion

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MinionsConfig {
    @Expose
    @ConfigOption(name = "Name Display", desc = "Show the minion name and tier over the minion.")
    @ConfigEditorBoolean
    @FeatureToggle
    var nameDisplay: Boolean = true

    @Expose
    @ConfigOption(name = "Only Tier", desc = "Show only the tier number over the minion. (Useful for Bingo)")
    @ConfigEditorBoolean
    var nameOnlyTier: Boolean = false

    @Expose
    @ConfigOption(
        name = "Minion Upgrade Helper",
        desc = "Add a button in the Minion menu to obtain required items for the next upgrade from Sacks or Bazaar.",
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var minionConfigHelper: Boolean = true

    @Expose
    @ConfigOption(name = "Last Clicked", desc = "")
    @Accordion
    var lastClickedMinion: LastClickedMinionConfig = LastClickedMinionConfig()

    @Expose
    @ConfigOption(name = "Emptied Time", desc = "")
    @Accordion
    var emptiedTime: EmptiedTimeConfig = EmptiedTimeConfig()

    @Expose
    @ConfigOption(
        name = "Hopper Profit Display",
        desc = "Use the hopper's held coins and the last empty time to calculate the coins per day.",
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hopperProfitDisplay: Boolean = true

    @Expose
    @ConfigOption(name = "Show XP", desc = "Show how much skill experience you will get when picking up items from the minion storage.")
    @ConfigEditorBoolean
    @FeatureToggle
    var xpDisplay: Boolean = true

    @Expose
    @ConfigLink(owner = MinionsConfig::class, field = "hopperProfitDisplay")
    var hopperProfitPos: Position = Position(360, 90, false, true)

    @Expose
    @ConfigOption(name = "Hide Mob Nametag", desc = "Hide the nametags of mobs close to minions.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideMobsNametagNearby: Boolean = false

    @Expose
    @ConfigOption(name = "Inferno Fuel Blocker", desc = "Prevent picking up the fuel or minion while there is active fuel.")
    @ConfigEditorBoolean
    @FeatureToggle
    var infernoFuelBlocker: Boolean = false
}
