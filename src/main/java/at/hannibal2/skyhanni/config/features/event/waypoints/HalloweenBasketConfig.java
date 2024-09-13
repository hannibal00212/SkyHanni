package at.hannibal2.skyhanni.config.features.event.waypoints;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HalloweenBasketConfig {

    @Expose
    @ConfigOption(name = "Basket Waypoints", desc = "Show all Halloween Basket waypoints.\n" +
        "Coordinates by §bTobbbb§7. (last updated: 2023)")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean allWaypoints = false;

    @Expose
    @ConfigOption(name = "Entrance Waypoints", desc = "Show helper waypoints to Baskets #23, #24, and #25.\n" +
        "Coordinates by §bErymanthus§7.")
    @ConfigEditorBoolean
    public boolean allEntranceWaypoints = false;

    @Expose
    @ConfigOption(name = "Only Closest", desc = "Only show the closest waypoint.")
    @ConfigEditorBoolean
    public boolean onlyClosest = true;
}
