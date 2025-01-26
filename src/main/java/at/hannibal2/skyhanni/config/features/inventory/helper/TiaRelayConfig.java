package at.hannibal2.skyhanni.config.features.inventory.helper;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TiaRelayConfig {

    @Expose
    @ConfigOption(name = "Sound Puzzle Helper", desc = "Help with solving the sound puzzle for Tia (the 9 Operator Chips to do maintenance for the Abiphone Network).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean soundHelper = true;

    @Expose
    @ConfigOption(name = "Next Waypoint", desc = "Show the next relay waypoint for Tia the Fairy, where maintenance for the Abiphone network needs to be done.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean nextWaypoint = true;

    @Expose
    @ConfigOption(name = "All Waypoints", desc = "Show all relay waypoints at once (intended for debugging).")
    @ConfigEditorBoolean
    public boolean allWaypoints = false;

    @Expose
    @ConfigOption(name = "Mute Sound", desc = "Mute the sound when close to the relay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tiaRelayMute = true;
}
