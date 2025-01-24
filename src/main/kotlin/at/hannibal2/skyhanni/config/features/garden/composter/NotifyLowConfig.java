package at.hannibal2.skyhanni.config.features.garden.composter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class NotifyLowConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show a notification when Organic Matter or Fuel runs low in your Composter.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Show Title", desc = "Send a title to notify.")
    @ConfigEditorBoolean
    public boolean title = false;

    @Expose
    @ConfigOption(name = "Min Organic Matter", desc = "Warn when Organic Matter is below this value.")
    @ConfigEditorSlider(
        minValue = 1_000,
        maxValue = 80_000,
        minStep = 1
    )
    public int organicMatter = 20_000;

    @Expose
    @ConfigOption(name = "Min Fuel", desc = "Warn when Fuel is below this value.")
    @ConfigEditorSlider(
        minValue = 500,
        maxValue = 40_000,
        minStep = 1
    )
    public int fuel = 10_000;
}
