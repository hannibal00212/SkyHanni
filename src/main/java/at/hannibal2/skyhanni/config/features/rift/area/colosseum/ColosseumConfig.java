package at.hannibal2.skyhanni.config.features.rift.area.colosseum;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ColosseumConfig {

    @Expose
    @ConfigOption(name = "Highlight Blobbercysts", desc = "Highlight Blobbercysts in Bacte fight.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightBlobbercysts = true;

    @Expose
    @ConfigOption(name = "Tentacle Waypoints", desc = "Show waypoints for tentacles with their HP in Bacte fight.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tentacleWaypoints = true;
}
