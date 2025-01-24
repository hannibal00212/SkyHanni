package at.hannibal2.skyhanni.config.features.misc.cosmetic;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FollowingLineConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Draw a colored line behind the player.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    public String lineColor = "0:255:255:255:255";

    @Expose
    @ConfigOption(name = "Time Alive", desc = "Time in seconds until the line fades out.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 30)
    public int secondsAlive = 3;

    @Expose
    @ConfigOption(name = "Max Line Width", desc = "Max width of the line.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
    public int lineWidth = 4;

    @Expose
    @ConfigOption(name = "Behind Blocks", desc = "Show behind blocks.")
    @ConfigEditorBoolean
    public boolean behindBlocks = false;
}
