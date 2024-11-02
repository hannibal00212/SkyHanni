package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ExcavatorTooltipHiderConfig {

    @Expose
    @ConfigOption(name = "Hide Dirt", desc = "Hides tooltips of the Dirt inside of the Fossil Excavator.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideDirt = true;

    @Expose
    @ConfigOption(name = "Hide Everything", desc = "Hide all tooltips inside of the Fossil Excavator.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideEverything = false;
}
