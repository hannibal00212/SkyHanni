package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestWaypointConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a waypoint of the next pest when using a vacuum. Uses the particles and math to detect the location from everywhere in the garden."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Hide Particles",
        desc = "Hide the particles of the ability."
    )
    @ConfigEditorBoolean
    public boolean hideParticles = true;

    @Expose
    @ConfigOption(
        name = "Draw Line",
        desc = "Draw a line to the waypoint."
    )
    @ConfigEditorBoolean
    public boolean drawLine = true;

    @Expose
    @ConfigOption(
        name = "Show Middle",
        desc = "Also show a waypoint to the middle of a plot. This can help determine if the tracker is pointing to the middle instead of a pest."
    )
    @ConfigEditorBoolean
    public boolean showMiddle = false;

    @Expose
    @ConfigOption(
        name = "Show For Seconds",
        desc = "The waypoint will disappear after this number of seconds."
    )
    @ConfigEditorSlider(minValue = 5, maxValue = 20, minStep = 1)
    public int showForSeconds = 15;
}
