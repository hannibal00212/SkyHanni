package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FishingBaitWarningsConfig {
    @Expose
    @ConfigOption(name = "Bait Change Warning", desc = "Show warning when fishing bait is changed")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean baitChangeWarning = false;

    @Expose
    @ConfigOption(name = "No Bait Warning", desc = "Show warning when no bait is used")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean noBaitWarning = false;
}
