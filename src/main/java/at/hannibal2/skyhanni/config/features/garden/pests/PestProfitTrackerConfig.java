package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestProfitTrackerConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Count all items you pick up when killing pests."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Hide messages",
        desc = "Hide regular pest drop messages."
    )
    @ConfigEditorBoolean
    public boolean hideChat = true;

    @Expose
    @ConfigOption(name = "Time Displayed", desc = "Time displayed after killing a pest.")
    @ConfigEditorSlider(minValue = 5, maxValue = 60, minStep = 1)
    public int timeDisplayed = 30;

    @Expose
    @ConfigLink(owner = PestProfitTrackerConfig.class, field = "enabled")
    public Position position = new Position(20, 20, false, true);
}
