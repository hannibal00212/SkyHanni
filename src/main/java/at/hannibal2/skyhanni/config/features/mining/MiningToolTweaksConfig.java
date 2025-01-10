package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiningToolTweaksConfig {
    @Expose
    @ConfigOption(name = "Break Down Stats", desc = "Allows you to show where some stats come from when pressing a key")
    @Accordion
    public BreakDownConfig BreakDownStats = new BreakDownConfig();

    @Expose
    @ConfigOption(name = "Better Stat Display", desc = "Shows the Drill part stat under the other stats instead of below")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean BetterStatDisplayDrills = true;

    @Expose
    @ConfigOption(name = "Hide Omelette Description", desc = "Hide the description of the omelette drill part")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean CompactDrillPart = true;


}
