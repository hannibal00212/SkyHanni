package at.hannibal2.skyhanni.config.features.rift.area.westvillage;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class KloonHackingConfig {

    @Expose
    @ConfigOption(name = "Hacking Solver", desc = "Highlight the correct button to click in the hacking inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean solver = true;

    @Expose
    @ConfigOption(name = "Color Guide", desc = "Show which color to pick.")
    @ConfigEditorBoolean
    @FeatureToggle
    // TODO rename to color
    public boolean colour = true;

    @Expose
    @ConfigOption(name = "Terminal Waypoints", desc = "While wearing the helmet, waypoints will appear at each terminal location.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean waypoints = true;
}
