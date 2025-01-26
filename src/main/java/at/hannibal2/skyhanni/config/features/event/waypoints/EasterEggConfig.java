package at.hannibal2.skyhanni.config.features.event.waypoints;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EasterEggConfig {

    @Expose
    @ConfigOption(name = "Egg Waypoints", desc = "Show all Easter Egg waypoints.\n" +
        "Coordinates by §bSorkoPiko§7. (last updated: 2024)")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean allWaypoints = false;

    @Expose
    @ConfigOption(name = "Entrance Waypoints", desc = "Show helper waypoints to Baskets #18, #27, and #30.\n" +
        "Coordinates by §bSorkoPiko§7 and §bErymanthus§7.")
    @ConfigEditorBoolean
    public boolean allEntranceWaypoints = false;

    @Expose
    @ConfigOption(name = "Only Closest", desc = "Only show the closest waypoint.")
    @ConfigEditorBoolean
    public boolean onlyClosest = true;
}
