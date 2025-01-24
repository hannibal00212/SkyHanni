package at.hannibal2.skyhanni.config.features.minion;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EmptiedTimeConfig {
    @Expose
    @ConfigOption(name = "Emptied Time Display", desc = "Show the time when the hopper in the minion was last emptied.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = false;

    @Expose
    @ConfigOption(
        name = "Distance",
        desc = "Maximum distance to display minion data."
    )
    @ConfigEditorSlider(
        minValue = 3,
        maxValue = 30,
        minStep = 1
    )
    public int distance = 10;
}
