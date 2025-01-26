package at.hannibal2.skyhanni.config.features.event.waypoints;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class HalloweenBasketConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show all Halloween Basket waypoints.\n" +
        "§eCoordinates may not always be up to date!")
    @ConfigEditorBoolean
    @FeatureToggle
    // TODO rename to "enabled"
    public boolean allWaypoints = false;

    @Expose
    @ConfigOption(name = "Only Closest", desc = "Only show the closest waypoint.")
    @ConfigEditorBoolean
    public boolean onlyClosest = true;

    @Expose
    @ConfigOption(name = "Pathfind", desc = "Show a path to the closest basket.")
    @ConfigEditorBoolean
    public Property<Boolean> pathfind = Property.of(true);
}
