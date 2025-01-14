package at.hannibal2.skyhanni.config.features.event.hoppity

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HoppityEggsConfig {
    @Expose
    @ConfigOption(name = "Hoppity Abiphone Calls", desc = "")
    @Accordion
    var hoppityCallWarning: HoppityCallWarningConfig = HoppityCallWarningConfig()

    @Expose
    @ConfigOption(name = "Hoppity Hunt Stats Summary", desc = "")
    @Accordion
    var eventSummary: HoppityEventSummaryConfig = HoppityEventSummaryConfig()

    @Expose
    @ConfigOption(name = "Warp Menu", desc = "")
    @Accordion
    var warpMenu: HoppityWarpMenuConfig = HoppityWarpMenuConfig()

    @Expose
    @ConfigOption(name = "Stray Timer", desc = "")
    @Accordion
    var strayTimer: HoppityStrayTimerConfig = HoppityStrayTimerConfig()

    @Expose
    @ConfigOption(name = "Chat Messages", desc = "")
    @Accordion
    var chat: HoppityChatConfig = HoppityChatConfig()

    @Expose
    @ConfigOption(name = "Egg Waypoints", desc = "")
    @Accordion
    var waypoints: HoppityWaypointsConfig = HoppityWaypointsConfig()

    @Expose
    @ConfigOption(name = "Egg Locations", desc = "")
    @Accordion
    var locations: HoppityLocationConfig = HoppityLocationConfig()

    @Expose
    @ConfigOption(name = "Unclaimed Eggs", desc = "")
    @Accordion
    var unclaimedEggs: HoppityUnclaimedEggsConfig = HoppityUnclaimedEggsConfig()

    @Expose
    @ConfigOption(
        name = "Adjust player opacity",
        desc = "Adjust the opacity of players near shared & guessed egg waypoints. (in %)"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 100f, minStep = 1f)
    var playerOpacity: Int = 40

    @Expose
    @ConfigOption(
        name = "Highlight Hoppity Shop",
        desc = "Highlight items that haven't been bought from the Hoppity shop yet."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightHoppityShop: Boolean = true

    @Expose
    @ConfigOption(name = "Hoppity Shop Reminder", desc = "Remind you to open the Hoppity Shop each year.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hoppityShopReminder: Boolean = true

    @Expose
    @ConfigOption(
        name = "Rabbit Pet Warning",
        desc = "Warn when using the Egglocator without a §d§lMythic Rabbit Pet §7equipped. " +
            "§eOnly enable this setting when you own a mythic Rabbit pet."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var petWarning: Boolean = false

    @Expose
    @ConfigOption(
        name = "Prevent Missing Rabbit the Fish",
        desc = "Prevent closing a Meal Egg's inventory if Rabbit the Fish is present."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var preventMissingRabbitTheFish: Boolean = true
}
