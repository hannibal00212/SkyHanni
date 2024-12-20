package at.hannibal2.skyhanni.config.features.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class GlaciteMineshaftConfig {
    @Expose
    @ConfigOption(name = "Mineshaft Waypoints", desc = "General waypoints inside the Mineshaft.")
    @Accordion
    public MineshaftWaypointsConfig mineshaftWaypoints = new MineshaftWaypointsConfig();

    @Expose
    @ConfigOption(name = "Corpse Locator", desc = "")
    @Accordion
    public CorpseLocatorConfig corpseLocator = new CorpseLocatorConfig();

    @Expose
    @ConfigOption(name = "Corpse Tracker", desc = "")
    @Accordion
    public CorpseTrackerConfig corpseTracker = new CorpseTrackerConfig();

    @Expose
    @ConfigOption(name = "Share Waypoint Location", desc = "Share the location of the nearest waypoint upon key press.\n" +
        "§eYou can share the location even if it has already been shared!")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int shareWaypointLocation = Keyboard.KEY_NONE;
}
