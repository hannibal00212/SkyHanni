package at.hannibal2.skyhanni.config.features.event.hoppity

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HoppityUnclaimedEggsConfig {
    @Expose
    @ConfigOption(
        name = "Show Unclaimed Eggs",
        desc = "Display which eggs haven't been found in the last SkyBlock day."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigLink(owner = HoppityUnclaimedEggsConfig::class, field = "enabled")
    var position: Position = Position(200, 120, false, true)

    @Expose
    @ConfigOption(name = "Unclaimed Eggs Order", desc = "Order in which to display unclaimed eggs.")
    @ConfigEditorDropdown
    var displayOrder: UnclaimedEggsOrder = UnclaimedEggsOrder.SOONEST_FIRST

    enum class UnclaimedEggsOrder(private val displayName: String) {
        SOONEST_FIRST("Soonest First"),
        MEAL_ORDER("Meal Order"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Show Collected Locations",
        desc = "Show the number of found egg locations on this island.\n" +
            "§eThis is not retroactive and may not be fully synced with Hypixel's count."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showCollectedLocationCount: Boolean = false

    @Expose
    @ConfigOption(
        name = "Show While Busy",
        desc = "Show while \"busy\" (in a farming contest, doing Kuudra, in the rift, etc)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showWhileBusy: Boolean = false

    @Expose
    @ConfigOption(name = "Show Outside SkyBlock", desc = "Show on Hypixel even when not playing SkyBlock.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showOutsideSkyblock: Boolean = false

    @Expose
    @ConfigOption(name = "Warn When Unclaimed", desc = "Warn when all six eggs are ready to be found.")
    @ConfigEditorBoolean
    @FeatureToggle
    var warningsEnabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Warn While Busy",
        desc = "Warn while \"busy\" (in a farming contest, doing Kuudra, in the rift, etc)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var warnWhileBusy: Boolean = false

    @Expose
    @ConfigOption(
        name = "Click to Warp",
        desc = "Make the eggs ready chat message & unclaimed timer display clickable to warp you to an island."
    )
    @ConfigEditorBoolean
    var warpClickEnabled: Boolean = false

    @Expose
    @ConfigOption(name = "Warp Destination", desc = "A custom island to warp to in the above option.")
    @ConfigEditorText
    var warpClickDestination: String = "nucleus"
}
