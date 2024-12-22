package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityEggsConfig {

    @Expose
    @ConfigOption(name = "Hoppity Abiphone Calls", desc = "")
    @Accordion
    public HoppityCallWarningConfig hoppityCallWarning = new HoppityCallWarningConfig();

    @Expose
    @ConfigOption(name = "Hoppity Hunt Stats Summary", desc = "")
    @Accordion
    public HoppityEventSummaryConfig eventSummary = new HoppityEventSummaryConfig();

    @Expose
    @ConfigOption(name = "Warp Menu", desc = "")
    @Accordion
    public HoppityWarpMenuConfig warpMenu = new HoppityWarpMenuConfig();

    @Expose
    @ConfigOption(name = "Stray Timer", desc = "")
    @Accordion
    public HoppityStrayTimerConfig strayTimer = new HoppityStrayTimerConfig();

    @Expose
    @ConfigOption(name = "Chat Messages", desc = "")
    @Accordion
    public HoppityChatConfig chat = new HoppityChatConfig();

    @Expose
    @ConfigOption(name = "Egg Waypoints", desc = "")
    @Accordion
    public HoppityWaypointsConfig waypoints = new HoppityWaypointsConfig();

    @Expose
    @ConfigOption(name = "Egg Locations", desc = "")
    @Accordion
    public HoppityLocationConfig locations = new HoppityLocationConfig();

    @Expose
    @ConfigOption(name = "Unclaimed Eggs", desc = "")
    @Accordion
    public HoppityUnclaimedEggsConfig unclaimedEggs = new HoppityUnclaimedEggsConfig();

    @Expose
    @ConfigOption(name = "Adjust player opacity", desc = "Adjust the opacity of players near shared & guessed egg waypoints. (in %)")
    @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 1)
    public int playerOpacity = 40;

    @Expose
    @ConfigOption(name = "Highlight Hoppity Shop", desc = "Highlight items that haven't been bought from the Hoppity shop yet.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightHoppityShop = true;

    @Expose
    @ConfigOption(name = "Hoppity Shop Reminder", desc = "Remind you to open the Hoppity Shop each year.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hoppityShopReminder = true;

    @Expose
    @ConfigOption(
        name = "Rabbit Pet Warning",
        desc = "Warn when using the Egglocator without a §d§lMythic Rabbit Pet §7equipped. " +
            "§eOnly enable this setting when you own a mythic Rabbit pet."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean petWarning = false;

    @Expose
    @ConfigOption(name = "Prevent Missing Rabbit the Fish", desc = "Prevent closing a Meal Egg's inventory if Rabbit the Fish is present.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean preventMissingRabbitTheFish = true;
}
