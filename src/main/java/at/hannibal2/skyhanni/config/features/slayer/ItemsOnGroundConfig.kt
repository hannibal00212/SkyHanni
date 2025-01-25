package at.hannibal2.skyhanni.config.features.slayer;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ItemsOnGroundConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the name and price of items laying on the ground. §cOnly in slayer areas!")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Minimum Price", desc = "Items below this price will be ignored.")
    @ConfigEditorSlider(minValue = 1, maxValue = 1_000_000, minStep = 1)
    public int minimumPrice = 50_000;
}
