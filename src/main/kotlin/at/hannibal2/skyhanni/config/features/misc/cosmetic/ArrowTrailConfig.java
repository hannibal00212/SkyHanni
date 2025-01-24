package at.hannibal2.skyhanni.config.features.misc.cosmetic;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ArrowTrailConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Draw a colored line behind arrows in the air.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Hide Non-player Arrows", desc = "Only show for arrows shot by players.")
    @ConfigEditorBoolean
    public boolean hideOtherArrows = true;

    @Expose
    @ConfigOption(name = "Arrow Color", desc = "Color of the line.")
    @ConfigEditorColour
    public String arrowColor = "0:200:85:255:85";

    @Expose
    @ConfigOption(name = "Player Arrows", desc = "Different color for the line of arrows that you have shot.")
    @ConfigEditorBoolean
    public boolean handlePlayerArrowsDifferently = false;

    @Expose
    @ConfigOption(name = "Player Arrow Color", desc = "Color of the line of your own arrows.")
    @ConfigEditorColour
    public String playerArrowColor = "0:200:85:255:255";

    @Expose
    @ConfigOption(name = "Time Alive", desc = "Time in seconds until the trail fades out.")
    @ConfigEditorSlider(minStep = 0.1f, minValue = 0.1f, maxValue = 10)
    public float secondsAlive = 0.5f;

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
    public int lineWidth = 4;
}
