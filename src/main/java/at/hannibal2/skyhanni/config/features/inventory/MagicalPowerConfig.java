package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MagicalPowerConfig {
    @Expose
    @ConfigOption(name = "Magical Power Display", desc = "Show Magical Power as stack size inside Accessory Bag and Auction House.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Colored", desc = "Whether to make the numbers colored.")
    @ConfigEditorBoolean
    public boolean colored = false;
}
