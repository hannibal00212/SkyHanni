package at.hannibal2.skyhanni.config.features.event.winter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class WinterConfig {

    @Expose
    @ConfigOption(name = "Frozen Treasure Tracker", desc = "")
    @Accordion
    public FrozenTreasureConfig frozenTreasureTracker = new FrozenTreasureConfig();

    @Accordion
    @Expose
    @ConfigOption(name = "Refined Bottle of Jyrre Timer", desc = "")
    public JyrreTimerConfig jyrreTimer = new JyrreTimerConfig();

    @Expose
    @ConfigOption(name = "Island Close Time", desc = "While on the Winter Island, show a timer until Jerry's Workshop closes.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean islandCloseTime = true;

    @Expose
    @ConfigLink(owner = WinterConfig.class, field = "islandCloseTime")
    public Position islandCloseTimePosition = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "New Year Cake Reminder", desc = "Send a reminder while the New Year Cake can be collected in the hub.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean newYearCakeReminder = true;

    @Expose
    @ConfigOption(name = "Reindrake Warp Helper", desc = "Sends a clickable message in chat to warp to the Winter Island spawn when a Reindrake spawns.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean reindrakeWarpHelper = true;

}
