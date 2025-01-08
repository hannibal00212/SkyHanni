package at.hannibal2.skyhanni.config.features.mining.glacite;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MineshaftWaypointsConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable features related to the Glacite Mineshaft.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Entrance Location", desc = "Mark the location of the entrance with a waypoint.")
    @ConfigEditorBoolean
    public boolean entranceLocation = false;

    @Expose
    @ConfigOption(name = "Ladder Location", desc = "Mark the location of the ladders at the bottom of the entrance with a waypoint.")
    @ConfigEditorBoolean
    public boolean ladderLocation = false;
}
