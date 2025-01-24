package at.hannibal2.skyhanni.config.features.mining.glacite;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FossilExcavatorSolverConfig {

    @Expose
    @ConfigOption(name = "Fossil Excavator Helper", desc = "Helper for finding fossils in the fossil excavator.\n" +
        "§eWill always solve if you have at least 18 clicks. Solves everything except Spine, Ugly and Helix in 16 clicks.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Show Percentage", desc = "Shows percentage chance that next click will be a fossil.\n" +
        "§eThis assumes there is a fossil hidden in the dirt.")
    @ConfigEditorBoolean
    public boolean showPercentage = true;

    @Expose
    @ConfigLink(owner = FossilExcavatorSolverConfig.class, field = "enabled")
    public Position position = new Position(183, 212, false, true);
}
