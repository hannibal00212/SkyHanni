package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityWaypointsConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggle guess waypoints for Hoppity's Hunt.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Show Waypoints Immediately",
        desc = "Show an estimated waypoint immediately after clicking.\n" +
            "§cThis might cause issues with other particle sources."
    )
    @ConfigEditorBoolean
    public boolean showImmediately = false;

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the waypoint.")
    @ConfigEditorColour
    public String color = "0:53:46:224:73";

    @Expose
    @ConfigOption(name = "Show Line", desc = "Show a line to the waypoint.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLine = false;

    @Expose
    @ConfigOption(name = "Show Path Finder", desc = "Show a pathfind to the next hoppity egg.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showPathFinder = false;

    @Expose
    @ConfigOption(name = "Show All Waypoints", desc = "Show all possible egg waypoints for the current lobby. §e" +
        "Only works when you don't have an Egglocator in your inventory.")
    @ConfigEditorBoolean
    public boolean showAll = false;

    @Expose
    @ConfigOption(name = "Hide Duplicate Waypoints", desc = "Hide egg waypoints you have already found.\n" +
        "§eOnly works when you don't have an Egglocator in your inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideDuplicates = false;
}
