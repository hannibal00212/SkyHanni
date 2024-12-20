package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PatcherCoordsWaypointConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight the coordinates sent by Patcher.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the waypoint.")
    @ConfigEditorColour
    public String color = "0:194:75:197:64";

    @Expose
    @ConfigOption(name = "Duration", desc = "Duration of the waypoint.")
    @ConfigEditorSlider(minStep = 5, maxValue = 120, minValue = 1)
    public int duration = 60;

}
